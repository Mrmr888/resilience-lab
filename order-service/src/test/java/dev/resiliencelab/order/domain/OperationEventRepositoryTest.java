package dev.resiliencelab.order.domain;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class OperationEventRepositoryTest {

    @Autowired
    private OperationEventRepository repository;

    @Test
    void shouldCountEventsByType() {
        repository.save(new OperationEvent(
                UUID.randomUUID().toString(),
                EventType.DOWNSTREAM_FAILURE,
                Severity.CRITICAL,
                "order-service",
                "fallback",
                UUID.randomUUID().toString(),
                12,
                Instant.now()));

        assertThat(repository.countByEventType(EventType.DOWNSTREAM_FAILURE)).isEqualTo(1);
    }
}
