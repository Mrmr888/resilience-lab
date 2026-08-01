package dev.resiliencelab.inventory.api;

import dev.resiliencelab.inventory.domain.FaultScenario;
import dev.resiliencelab.inventory.service.FaultScenarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/operations/scenario")
public class OperationsController {

    private final FaultScenarioService faultScenarioService;

    public OperationsController(FaultScenarioService faultScenarioService) {
        this.faultScenarioService = faultScenarioService;
    }

    @GetMapping
    public FaultScenario current() {
        return faultScenarioService.current();
    }

    @PutMapping
    public FaultScenario update(@Valid @RequestBody UpdateScenarioRequest request) {
        return faultScenarioService.update(request.mode(), request.delayMs());
    }
}

