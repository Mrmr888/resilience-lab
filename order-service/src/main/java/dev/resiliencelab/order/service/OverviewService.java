package dev.resiliencelab.order.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import dev.resiliencelab.order.domain.EventType;
import dev.resiliencelab.order.domain.OperationEvent;
import dev.resiliencelab.order.domain.OperationEventRepository;
import dev.resiliencelab.order.web.EventView;
import dev.resiliencelab.order.web.OverviewResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class OverviewService {

    private final OperationEventRepository eventRepository;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public OverviewService(
            OperationEventRepository eventRepository,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        this.eventRepository = eventRepository;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    public OverviewResponse overview() {
        long total = eventRepository.count();
        long successful = eventRepository.countByEventType(EventType.ORDER_SUCCEEDED);
        long failed = eventRepository.countByEventType(EventType.DOWNSTREAM_FAILURE);
        double successRate = total == 0 ? 100.0 : Math.round(successful * 10_000.0 / total) / 100.0;
        List<OperationEvent> recent = recentEntities(100);

        return new OverviewResponse(
                4,
                total,
                successful,
                failed,
                successRate,
                percentile95(recent),
                circuitBreakerRegistry.circuitBreaker("inventory").getState().name(),
                Instant.now());
    }

    public List<EventView> recentEvents(int limit) {
        int boundedLimit = Math.max(1, Math.min(limit, 100));
        return recentEntities(boundedLimit).stream().map(EventView::from).toList();
    }

    private List<OperationEvent> recentEntities(int limit) {
        return eventRepository.findAll(PageRequest.of(
                        0,
                        limit,
                        Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent();
    }

    private long percentile95(List<OperationEvent> events) {
        if (events.isEmpty()) {
            return 0;
        }
        List<Long> latencies = new ArrayList<>(events.stream()
                .map(OperationEvent::getLatencyMs)
                .toList());
        latencies.sort(Long::compareTo);
        int index = Math.max(0, (int) Math.ceil(latencies.size() * 0.95) - 1);
        return latencies.get(index);
    }
}

