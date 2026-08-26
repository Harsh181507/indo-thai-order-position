package com.pm.orderupdateservice.service;

import com.pm.orderupdateservice.client.PositionServiceClient;
import com.pm.orderupdateservice.model.OrderEvent;
import com.pm.orderupdateservice.model.TransactionType;
import com.pm.orderupdateservice.throttle.EventThrottle;
import com.pm.orderupdateservice.validation.OrderEventValidator;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

class OrderProcessingConcurrencyTest {

    @Test
    void shouldProcessUniqueEventsConcurrently() throws InterruptedException {
        OrderEventValidator validator = new OrderEventValidator();
        PositionServiceClient positionServiceClient = mock(PositionServiceClient.class);
        EventThrottle eventThrottle = mock(EventThrottle.class);

        OrderProcessingService service = new OrderProcessingService(
                validator,
                positionServiceClient,
                eventThrottle
        );

        int eventCount = 100;

        try (ExecutorService executorService = Executors.newFixedThreadPool(10)) {
            for (int i = 0; i < eventCount; i++) {
                int eventNumber = i;

                executorService.submit(() -> service.process(
                        new OrderEvent(
                                "evt-concurrent-" + eventNumber,
                                "RELIANCE",
                                TransactionType.BUY,
                                10
                        )
                ));
            }

            executorService.shutdown();

            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Concurrent processing did not complete");
            }
        }

        verify(positionServiceClient, times(eventCount)).send(
                org.mockito.ArgumentMatchers.any(OrderEvent.class)
        );

        verify(eventThrottle, times(eventCount)).acquire();
    }

    @Test
    void shouldProcessDuplicateEventOnlyOnceConcurrently() throws InterruptedException {
        OrderEventValidator validator = new OrderEventValidator();
        PositionServiceClient positionServiceClient = mock(PositionServiceClient.class);
        EventThrottle eventThrottle = mock(EventThrottle.class);

        OrderProcessingService service = new OrderProcessingService(
                validator,
                positionServiceClient,
                eventThrottle
        );

        OrderEvent event = new OrderEvent(
                "evt-same",
                "RELIANCE",
                TransactionType.BUY,
                100
        );

        int requestCount = 100;

        try (ExecutorService executorService = Executors.newFixedThreadPool(10)) {
            for (int i = 0; i < requestCount; i++) {
                executorService.submit(() -> service.process(event));
            }

            executorService.shutdown();

            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Concurrent processing did not complete");
            }
        }

        verify(positionServiceClient, times(1)).send(event);
        verify(eventThrottle, times(1)).acquire();
    }
}