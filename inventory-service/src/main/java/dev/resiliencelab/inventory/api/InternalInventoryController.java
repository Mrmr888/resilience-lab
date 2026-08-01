package dev.resiliencelab.inventory.api;

import dev.resiliencelab.inventory.service.FaultScenarioService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/internal/inventory")
public class InternalInventoryController {

    private final FaultScenarioService faultScenarioService;

    public InternalInventoryController(FaultScenarioService faultScenarioService) {
        this.faultScenarioService = faultScenarioService;
    }

    @GetMapping("/{sku}")
    public InventoryResponse check(
            @PathVariable @Pattern(regexp = "[A-Za-z0-9-]{2,32}") String sku,
            @RequestParam(defaultValue = "1") @Min(1) @Max(20) int quantity) {
        faultScenarioService.applyCurrentFault();
        int stock = 5 + Math.floorMod(sku.toUpperCase().hashCode(), 96);
        boolean available = stock >= quantity;
        int remaining = available ? stock - quantity : stock;
        return new InventoryResponse(sku.toUpperCase(), quantity, remaining, available);
    }
}

