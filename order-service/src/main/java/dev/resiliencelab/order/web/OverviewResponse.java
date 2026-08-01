package dev.resiliencelab.order.web;

import java.time.Instant;

public record OverviewResponse(
        int serviceCount,
        long totalRequests,
        long successfulRequests,
        long failedRequests,
        double successRate,
        long p95LatencyMs,
        String circuitState,
        Instant generatedAt) {
}

