package dev.resiliencelab.order.service;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

import dev.resiliencelab.order.client.InventoryClient;
import dev.resiliencelab.order.domain.EventType;
import dev.resiliencelab.order.domain.OperationEvent;
import dev.resiliencelab.order.domain.OperationEventRepository;
import dev.resiliencelab.order.domain.Severity;
import dev.resiliencelab.order.web.InventoryResponse;
import dev.resiliencelab.order.web.OrderResult;
import dev.resiliencelab.order.web.SimulateOrderRequest;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderSimulationService {

    private static final String SOURCE_SERVICE = "order-service";

    private final InventoryClient inventoryClient;
    private final OperationEventRepository eventRepository;
    private final CircuitBreakerFactory<?, ?> circuitBreakerFactory;

    public OrderSimulationService(
            InventoryClient inventoryClient,
            OperationEventRepository eventRepository,
            CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
        this.inventoryClient = inventoryClient;
        this.eventRepository = eventRepository;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    public OrderResult simulate(SimulateOrderRequest request, String incomingTraceId) {
        String traceId = normalizeTraceId(incomingTraceId);
        long startedAt = System.nanoTime();
        Supplier<OrderResult> operation = () -> callInventory(request, traceId, startedAt);

        return circuitBreakerFactory.create("inventory").run(
                operation,
                throwable -> degraded(traceId, startedAt));
    }

    private OrderResult callInventory(SimulateOrderRequest request, String traceId, long startedAt) {
        InventoryResponse inventory = inventoryClient.check(request.sku(), request.quantity());
        long latencyMs = elapsedMillis(startedAt);
        if (!inventory.available()) {
            String message = "库存不足，订单演练已拒绝";
            saveEvent(EventType.ORDER_REJECTED, Severity.WARN, message, traceId, latencyMs);
            return new OrderResult("REJECTED", traceId, message, latencyMs, inventory);
        }

        String message = "库存校验成功，订单演练已完成";
        saveEvent(EventType.ORDER_SUCCEEDED, Severity.INFO, message, traceId, latencyMs);
        return new OrderResult("SUCCEEDED", traceId, message, latencyMs, inventory);
    }

    private OrderResult degraded(String traceId, long startedAt) {
        long latencyMs = elapsedMillis(startedAt);
        String message = "库存服务不可用，熔断降级已生效";
        saveEvent(EventType.DOWNSTREAM_FAILURE, Severity.CRITICAL, message, traceId, latencyMs);
        return new OrderResult("DEGRADED", traceId, message, latencyMs, null);
    }

    private void saveEvent(
            EventType eventType,
            Severity severity,
            String message,
            String traceId,
            long latencyMs) {
        eventRepository.save(new OperationEvent(
                UUID.randomUUID().toString(),
                eventType,
                severity,
                SOURCE_SERVICE,
                message,
                traceId,
                latencyMs,
                Instant.now()));
    }

    private long elapsedMillis(long startedAt) {
        return Math.max(0, (System.nanoTime() - startedAt) / 1_000_000);
    }

    private String normalizeTraceId(String incomingTraceId) {
        if (incomingTraceId != null && incomingTraceId.matches("[A-Za-z0-9-]{1,64}")) {
            return incomingTraceId;
        }
        return UUID.randomUUID().toString();
    }
}

