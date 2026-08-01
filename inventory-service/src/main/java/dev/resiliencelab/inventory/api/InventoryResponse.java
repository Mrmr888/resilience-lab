package dev.resiliencelab.inventory.api;

public record InventoryResponse(String sku, int requested, int remaining, boolean available) {
}

