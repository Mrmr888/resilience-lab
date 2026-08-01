package dev.resiliencelab.order.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SimulateOrderRequest(
        @NotBlank @Pattern(regexp = "[A-Za-z0-9-]{2,32}") String sku,
        @Min(1) @Max(20) int quantity) {
}

