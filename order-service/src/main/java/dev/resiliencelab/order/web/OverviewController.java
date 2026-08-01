package dev.resiliencelab.order.web;

import java.util.List;

import dev.resiliencelab.order.service.OverviewService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OverviewController {

    private final OverviewService overviewService;

    public OverviewController(OverviewService overviewService) {
        this.overviewService = overviewService;
    }

    @GetMapping("/overview")
    public OverviewResponse overview() {
        return overviewService.overview();
    }

    @GetMapping("/events")
    public List<EventView> events(@RequestParam(defaultValue = "20") int limit) {
        return overviewService.recentEvents(limit);
    }
}

