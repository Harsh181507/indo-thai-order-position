package com.pm.positionservice.model;

public record OrderEvent(
        String eventId,
        String symbol,
        TransactionType transactionType,
        Integer quantity
) {
}