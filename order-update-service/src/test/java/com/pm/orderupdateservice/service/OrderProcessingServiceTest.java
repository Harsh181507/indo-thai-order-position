package com.pm.orderupdateservice.service;

import com.pm.orderupdateservice.client.PositionServiceClient;
import com.pm.orderupdateservice.model.OrderEvent;
import com.pm.orderupdateservice.model.TransactionType;
import com.pm.orderupdateservice.throttle.EventThrottle;
import com.pm.orderupdateservice.validation.OrderEventValidator;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class OrderProcessingServiceTest {

    private final OrderEventValidator validator = new OrderEventValidator();
    private final PositionServiceClient positionServiceClient = mock(PositionServiceClient.class);
    private final EventThrottle eventThrottle = mock(EventThrottle.class);

    private final OrderProcessingService service = new OrderProcessingService(
            validator,
            positionServiceClient,
            eventThrottle
    );

    @Test
    void shouldProcessValidEvent() {
        OrderEvent event = new OrderEvent(
                "evt-0001",
                "RELIANCE",
                TransactionType.BUY,
                90
        );

        service.process(event);

        verify(eventThrottle).acquire();
        verify(positionServiceClient).send(event);
    }

    @Test
    void shouldIgnoreInvalidEvent() {
        OrderEvent event = new OrderEvent(
                "",
                "RELIANCE",
                TransactionType.BUY,
                90
        );

        service.process(event);

        verifyNoInteractions(positionServiceClient, eventThrottle);
    }

    @Test
    void shouldIgnoreDuplicateEvent() {
        OrderEvent event = new OrderEvent(
                "evt-0001",
                "RELIANCE",
                TransactionType.BUY,
                90
        );

        service.process(event);
        service.process(event);

        verify(eventThrottle).acquire();
        verify(positionServiceClient).send(event);
    }
}