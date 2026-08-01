package dev.resiliencelab.inventory.service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import dev.resiliencelab.inventory.domain.FaultMode;
import dev.resiliencelab.inventory.domain.FaultScenario;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FaultScenarioService {

    private static final int MAX_DELAY_MS = 2500;

    private final AtomicReference<FaultScenario> scenario = new AtomicReference<>(
            new FaultScenario(FaultMode.NORMAL, 0, Instant.now()));
    private final AtomicInteger flakyCounter = new AtomicInteger();

    public FaultScenario current() {
        return scenario.get();
    }

    public FaultScenario update(FaultMode mode, int delayMs) {
        if (delayMs < 0 || delayMs > MAX_DELAY_MS) {
            throw new IllegalArgumentException("delayMs must be between 0 and 2500");
        }
        int effectiveDelay = mode == FaultMode.SLOW ? delayMs : 0;
        FaultScenario updated = new FaultScenario(mode, effectiveDelay, Instant.now());
        scenario.set(updated);
        flakyCounter.set(0);
        return updated;
    }

    public void applyCurrentFault() {
        FaultScenario current = scenario.get();
        switch (current.mode()) {
            case NORMAL -> {
                return;
            }
            case SLOW -> sleep(current.delayMs());
            case ERROR -> throw unavailable("Injected inventory failure");
            case FLAKY -> {
                if (flakyCounter.incrementAndGet() % 2 == 0) {
                    throw unavailable("Injected intermittent inventory failure");
                }
            }
        }
    }

    private void sleep(int delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw unavailable("Inventory request was interrupted");
        }
    }

    private ResponseStatusException unavailable(String message) {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}

