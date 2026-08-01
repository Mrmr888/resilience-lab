package dev.resiliencelab.order.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationEventRepository extends JpaRepository<OperationEvent, String> {

    long countByEventType(EventType eventType);
}

