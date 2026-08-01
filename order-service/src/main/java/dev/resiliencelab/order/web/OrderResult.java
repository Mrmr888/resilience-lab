package dev.resiliencelab.order.web;

public record OrderResult(
        String outcome,
        String traceId,
        String message,
        long latencyMs,
        InventoryResponse inventory) {
}

