package dev.resiliencelab.order.web;

import java.time.Instant;

import dev.resiliencelab.order.domain.EventType;
import dev.resiliencelab.order.domain.OperationEvent;
import dev.resiliencelab.order.domain.Severity;

public record EventView(
        String id,
        EventType type,
        Severity severity,
        String sourceService,
        String message,
        String traceId,
        long latencyMs,
        Instant createdAt) {

    public static EventView from(OperationEvent event) {
        return new EventView(
                event.getId(),
                event.getEventType(),
                event.getSeverity(),
                event.getSourceService(),
                event.getMessage(),
                event.getTraceId(),
                event.getLatencyMs(),
                event.getCreatedAt());
    }
}

