package com.pm.orderupdateservice.validation;

import com.pm.orderupdateservice.model.OrderEvent;
import org.springframework.stereotype.Component;

@Component
public class OrderEventValidator {

    public ValidationResult validate(OrderEvent event) {
        if (event == null) {
            return ValidationResult.invalid("event must not be null");
        }

        if (event.eventId() == null || event.eventId().isBlank()) {
            return ValidationResult.invalid("event_id must not be blank");
        }

        if (event.symbol() == null || event.symbol().isBlank()) {
            return ValidationResult.invalid("symbol must not be blank");
        }

        if (event.transactionType() == null) {
            return ValidationResult.invalid("transaction_type must be BUY or SELL");
        }

        if (event.quantity() == null) {
            return ValidationResult.invalid("quantity must be an integer");
        }

        if (event.quantity() <= 0) {
            return ValidationResult.invalid("quantity must be positive");
        }

        return ValidationResult.success();
    }
}