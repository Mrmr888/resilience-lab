CREATE TABLE operation_event (
    id VARCHAR(36) PRIMARY KEY,
    event_type VARCHAR(40) NOT NULL,
    severity VARCHAR(16) NOT NULL,
    source_service VARCHAR(64) NOT NULL,
    message VARCHAR(255) NOT NULL,
    trace_id VARCHAR(64) NOT NULL,
    latency_ms BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_operation_event_created_at ON operation_event (created_at DESC);
CREATE INDEX idx_operation_event_type ON operation_event (event_type);

