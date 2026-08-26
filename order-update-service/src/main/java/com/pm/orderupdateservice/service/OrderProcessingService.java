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
    private final ProcessingStatistics statistics;
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    public OrderProcessingService(
            OrderEventValidator validator,
            PositionServiceClient positionServiceClient,
            EventThrottle eventThrottle,
            ProcessingStatistics statistics
    ) {
        this.validator = validator;
        this.positionServiceClient = positionServiceClient;
        this.eventThrottle = eventThrottle;
        this.statistics = statistics;
    }

    public void process(OrderEvent event) {
        statistics.recordTotal();

        ValidationResult validationResult = validator.validate(event);

        if (!validationResult.valid()) {
            statistics.recordInvalid();
            return;
        }

        if (!processedEventIds.add(event.eventId())) {
            statistics.recordDuplicate();
            return;
        }

        try {
            eventThrottle.acquire();
            positionServiceClient.send(event);
            statistics.recordSuccess();
        } catch (RuntimeException exception) {
            statistics.recordFailure();
            processedEventIds.remove(event.eventId());
            throw exception;
        }
    }
}