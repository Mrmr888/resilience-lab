package dev.resiliencelab.order.web;

public record InventoryResponse(String sku, int requested, int remaining, boolean available) {
}

