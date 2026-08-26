package com.pm.positionservice.integration;

import com.pm.positionservice.model.OrderEvent;
import com.pm.positionservice.model.TransactionType;
import com.pm.positionservice.service.PositionService;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class PositionServiceConcurrencyTest {

    @Test
    void shouldHandleConcurrentPositionUpdates() throws InterruptedException {
        PositionService positionService = new PositionService();

        int eventCount = 1000;

        try (ExecutorService executorService = Executors.newFixedThreadPool(10)) {
            for (int i = 0; i < eventCount; i++) {
                int eventNumber = i;

                executorService.submit(() -> positionService.process(
                        new OrderEvent(
                                "evt-" + eventNumber,
                                "RELIANCE",
                                TransactionType.BUY,
                                1
                        )
                ));
            }

            executorService.shutdown();
            assertThat(executorService.awaitTermination(10, TimeUnit.SECONDS))
                    .isTrue();
        }

        assertThat(positionService.getPositions())
                .containsEntry("RELIANCE", 1000L);
    }
}