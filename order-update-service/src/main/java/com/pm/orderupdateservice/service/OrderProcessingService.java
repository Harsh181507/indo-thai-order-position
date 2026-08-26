package com.pm.orderupdateservice.service;

import com.pm.orderupdateservice.client.PositionServiceClient;
import com.pm.orderupdateservice.model.OrderEvent;
import com.pm.orderupdateservice.throttle.EventThrottle;
import com.pm.orderupdateservice.validation.OrderEventValidator;
import com.pm.orderupdateservice.validation.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderProcessingService {

    private final OrderEventValidator validator;
    private final PositionServiceClient positionServiceClient;
    private final EventThrottle eventThrottle;
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    public OrderProcessingService(
            OrderEventValidator validator,
            PositionServiceClient positionServiceClient,
            EventThrottle eventThrottle
    ) {
        this.validator = validator;
        this.positionServiceClient = positionServiceClient;
        this.eventThrottle = eventThrottle;
    }

    public void process(OrderEvent event) {
        ValidationResult validationResult = validator.validate(event);

        if (!validationResult.valid()) {
            return;
        }

        if (!processedEventIds.add(event.eventId())) {
            return;
        }

        eventThrottle.acquire();
        positionServiceClient.send(event);
    }
}