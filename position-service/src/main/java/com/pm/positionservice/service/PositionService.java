package com.pm.positionservice.service;

import com.pm.positionservice.model.OrderEvent;
import com.pm.positionservice.model.TransactionType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PositionService {

    private final Map<String, Long> positions = new ConcurrentHashMap<>();
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    public void process(OrderEvent event) {
        if (!processedEventIds.add(event.eventId())) {
            return;
        }

        long positionChange = event.transactionType() == TransactionType.BUY
                ? event.quantity()
                : -event.quantity();

        positions.merge(event.symbol(), positionChange, Long::sum);
    }

    public Map<String, Long> getPositions() {
        return new HashMap<>(positions);
    }

    public void clear() {
        positions.clear();
        processedEventIds.clear();
    }
}