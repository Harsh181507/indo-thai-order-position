package com.pm.orderupdateservice.model;

public record OrderEvent(
        String eventId,
        String symbol,
        TransactionType transactionType,
        Integer quantity
) {
}