package dev.resiliencelab.inventory.api;

import dev.resiliencelab.inventory.domain.FaultMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateScenarioRequest(
        @NotNull FaultMode mode,
        @Min(0) @Max(2500) int delayMs) {
}

