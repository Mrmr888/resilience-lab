package dev.resiliencelab.order.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "operation_event")
public class OperationEvent {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 40, nullable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private Severity severity;

    @Column(name = "source_service", length = 64, nullable = false)
    private String sourceService;

    @Column(length = 255, nullable = false)
    private String message;

    @Column(name = "trace_id", length = 64, nullable = false)
    private String traceId;

    @Column(name = "latency_ms", nullable = false)
    private long latencyMs;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected OperationEvent() {
    }

    public OperationEvent(
            String id,
            EventType eventType,
            Severity severity,
            String sourceService,
            String message,
            String traceId,
            long latencyMs,
            Instant createdAt) {
        this.id = id;
        this.eventType = eventType;
        this.severity = severity;
        this.sourceService = sourceService;
        this.message = message;
        this.traceId = traceId;
        this.latencyMs = latencyMs;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public EventType getEventType() {
        return eventType;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getSourceService() {
        return sourceService;
    }

    public String getMessage() {
        return message;
    }

    public String getTraceId() {
        return traceId;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

