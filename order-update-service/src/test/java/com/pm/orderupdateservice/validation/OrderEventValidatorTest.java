package com.pm.orderupdateservice.validation;

import com.pm.orderupdateservice.model.OrderEvent;
import com.pm.orderupdateservice.model.TransactionType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderEventValidatorTest {

    private final OrderEventValidator validator = new OrderEventValidator();

    @Test
    void shouldAcceptValidBuyEvent() {
        OrderEvent event = new OrderEvent(
                "evt-0001",
                "RELIANCE",
                TransactionType.BUY,
                90
        );

        ValidationResult result = validator.validate(event);

        assertThat(result.valid()).isTrue();
    }

    @Test
    void shouldRejectBlankEventId() {
        OrderEvent event = new OrderEvent(
                "",
                "RELIANCE",
                TransactionType.BUY,
                90
        );

        ValidationResult result = validator.validate(event);

        assertThat(result.valid()).isFalse();
        assertThat(result.reason()).isEqualTo("event_id must not be blank");
    }

    @Test
    void shouldRejectBlankSymbol() {
        OrderEvent event = new OrderEvent(
                "evt-0001",
                "",
                TransactionType.BUY,
                90
        );

        ValidationResult result = validator.validate(event);

        assertThat(result.valid()).isFalse();
        assertThat(result.reason()).isEqualTo("symbol must not be blank");
    }

    @Test
    void shouldRejectMissingTransactionType() {
        OrderEvent event = new OrderEvent(
                "evt-0001",
                "RELIANCE",
                null,
                90
        );

        ValidationResult result = validator.validate(event);

        assertThat(result.valid()).isFalse();
        assertThat(result.reason()).isEqualTo("transaction_type must be BUY or SELL");
    }

    @Test
    void shouldRejectNullQuantity() {
        OrderEvent event = new OrderEvent(
                "evt-0001",
                "RELIANCE",
                TransactionType.BUY,
                null
        );

        ValidationResult result = validator.validate(event);

        assertThat(result.valid()).isFalse();
        assertThat(result.reason()).isEqualTo("quantity must be an integer");
    }

    @Test
    void shouldRejectZeroQuantity() {
        OrderEvent event = new OrderEvent(
                "evt-0001",
                "RELIANCE",
                TransactionType.BUY,
                0
        );

        ValidationResult result = validator.validate(event);

        assertThat(result.valid()).isFalse();
        assertThat(result.reason()).isEqualTo("quantity must be positive");
    }

    @Test
    void shouldRejectNegativeQuantity() {
        OrderEvent event = new OrderEvent(
                "evt-0001",
                "RELIANCE",
                TransactionType.BUY,
                -10
        );

        ValidationResult result = validator.validate(event);

        assertThat(result.valid()).isFalse();
        assertThat(result.reason()).isEqualTo("quantity must be positive");
    }

    @Test
    void shouldAcceptValidSellEvent() {
        OrderEvent event = new OrderEvent(
                "evt-0002",
                "TCS",
                TransactionType.SELL,
                75
        );

        ValidationResult result = validator.validate(event);

        assertThat(result.valid()).isTrue();
    }
}