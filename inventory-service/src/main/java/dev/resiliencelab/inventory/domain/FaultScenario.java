package dev.resiliencelab.inventory.domain;

import java.time.Instant;

public record FaultScenario(FaultMode mode, int delayMs, Instant updatedAt) {
}

