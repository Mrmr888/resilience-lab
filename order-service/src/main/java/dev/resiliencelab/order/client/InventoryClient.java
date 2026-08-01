package dev.resiliencelab.order.client;

import dev.resiliencelab.order.web.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @GetMapping("/internal/inventory/{sku}")
    InventoryResponse check(@PathVariable String sku, @RequestParam int quantity);
}

