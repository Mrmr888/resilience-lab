package dev.resiliencelab.order.web;

import dev.resiliencelab.order.service.OrderSimulationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderSimulationService orderSimulationService;

    public OrderController(OrderSimulationService orderSimulationService) {
        this.orderSimulationService = orderSimulationService;
    }

    @PostMapping("/simulate")
    public OrderResult simulate(
            @Valid @RequestBody SimulateOrderRequest request,
            @RequestHeader(name = "X-Trace-Id", required = false) String traceId) {
        return orderSimulationService.simulate(request, traceId);
    }
}

