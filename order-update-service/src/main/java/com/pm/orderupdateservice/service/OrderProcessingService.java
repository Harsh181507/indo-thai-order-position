package com.pm.orderupdateservice.service;

import com.pm.orderupdateservice.client.PositionServiceClient;
import com.pm.orderupdateservice.model.OrderEvent;
import com.pm.orderupdateservice.throttle.EventThrottle;
import com.pm.orderupdateservice.validation.OrderEventValidator;
import com.pm.orderupdateservice.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderProcessingService {

    private static final Logger log =
            LoggerFactory.getLogger(OrderProcessingService.class);

    private final OrderEventValidator validator;
    private final PositionServiceClient positionServiceClient;
    private final EventThrottle eventThrottle;
    private final ProcessingStatistics statistics;

    private final Set<String> processedEventIds =
            ConcurrentHashMap.newKeySet();

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

            log.warn(
                    "Rejected event: eventId={}, symbol={}, reason={}",
                    event == null ? null : event.eventId(),
                    event == null ? null : event.symbol(),
                    validationResult.reason()
            );

            return;
        }

        if (!processedEventIds.add(event.eventId())) {

            statistics.recordDuplicate();

            log.info(
                    "Duplicate event skipped: eventId={}",
                    event.eventId()
            );

            return;
        }

        try {

            eventThrottle.acquire();

            positionServiceClient.send(event);

            statistics.recordSuccess();

            log.info(
                    "Event successfully sent: eventId={}, symbol={}, transactionType={}, quantity={}",
                    event.eventId(),
                    event.symbol(),
                    event.transactionType(),
                    event.quantity()
            );

        } catch (RuntimeException exception) {

            statistics.recordFailure();

            /*
             * Remove the event ID so that the event can be retried
             * if processing is triggered again.
             */
            processedEventIds.remove(event.eventId());

            log.error(
                    "Failed to send event: eventId={}, symbol={}, reason={}",
                    event.eventId(),
                    event.symbol(),
                    exception.getMessage(),
                    exception
            );

            /*
             * Important:
             * Do NOT throw the exception here.
             *
             * A failure for one CSV row must not stop processing
             * of all remaining rows.
             */
        }
    }
}