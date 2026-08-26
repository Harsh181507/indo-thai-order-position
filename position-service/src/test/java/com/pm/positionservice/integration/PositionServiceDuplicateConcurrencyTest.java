package com.pm.positionservice.integration;

import com.pm.positionservice.model.OrderEvent;
import com.pm.positionservice.model.TransactionType;
import com.pm.positionservice.service.PositionService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class PositionServiceDuplicateConcurrencyTest {

    @Test
    void shouldProcessDuplicateEventOnlyOnceConcurrently() throws InterruptedException {
        PositionService positionService = new PositionService();

        OrderEvent event = new OrderEvent(
                "evt-duplicate",
                "RELIANCE",
                TransactionType.BUY,
                100
        );

        int requestCount = 100;

        try (ExecutorService executorService = Executors.newFixedThreadPool(10)) {
            for (int i = 0; i < requestCount; i++) {
                executorService.submit(() -> positionService.process(event));
            }

            executorService.shutdown();

            assertThat(executorService.awaitTermination(10, TimeUnit.SECONDS))
                    .isTrue();
        }

        assertThat(positionService.getPositions())
                .containsEntry("RELIANCE", 100L);
    }
}