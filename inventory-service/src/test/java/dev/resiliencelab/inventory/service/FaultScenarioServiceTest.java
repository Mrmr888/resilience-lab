package dev.resiliencelab.inventory.service;

import dev.resiliencelab.inventory.domain.FaultMode;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FaultScenarioServiceTest {

    private final FaultScenarioService service = new FaultScenarioService();

    @Test
    void shouldBoundInjectedDelay() {
        assertThatThrownBy(() -> service.update(FaultMode.SLOW, 2501))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void flakyModeShouldFailEverySecondCall() {
        service.update(FaultMode.FLAKY, 0);

        service.applyCurrentFault();
        assertThatThrownBy(service::applyCurrentFault)
                .isInstanceOf(ResponseStatusException.class);
        service.applyCurrentFault();
    }

    @Test
    void nonSlowModesShouldDiscardDelay() {
        assertThat(service.update(FaultMode.ERROR, 500).delayMs()).isZero();
    }
}

