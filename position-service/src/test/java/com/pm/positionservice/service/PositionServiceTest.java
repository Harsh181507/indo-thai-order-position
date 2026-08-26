package com.pm.positionservice.service;

import com.pm.positionservice.model.OrderEvent;
import com.pm.positionservice.model.TransactionType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PositionServiceTest {

    private final PositionService positionService = new PositionService();

    @Test
    void shouldIncreasePositionForBuyOrder() {
        OrderEvent event = new OrderEvent(
                "evt-0001",
                "RELIANCE",
                TransactionType.BUY,
                90
        );

        positionService.process(event);

        assertThat(positionService.getPositions())
                .containsEntry("RELIANCE", 90L);
    }

    @Test
    void shouldDecreasePositionForSellOrder() {
        OrderEvent event = new OrderEvent(
                "evt-0001",
                "TCS",
                TransactionType.SELL,
                75
        );

        positionService.process(event);

        assertThat(positionService.getPositions())
                .containsEntry("TCS", -75L);
    }

    @Test
    void shouldSupportMultipleSymbols() {
        positionService.process(new OrderEvent(
                "evt-0001",
                "RELIANCE",
                TransactionType.BUY,
                100
        ));

        positionService.process(new OrderEvent(
                "evt-0002",
                "TCS",
                TransactionType.SELL,
                75
        ));

        Map<String, Long> positions = positionService.getPositions();

        assertThat(positions)
                .containsEntry("RELIANCE", 100L)
                .containsEntry("TCS", -75L);
    }

    @Test
    void shouldKeepZeroPositionForSymbol() {
        positionService.process(new OrderEvent(
                "evt-0001",
                "INFY",
                TransactionType.BUY,
                100
        ));

        positionService.process(new OrderEvent(
                "evt-0002",
                "INFY",
                TransactionType.SELL,
                100
        ));

        assertThat(positionService.getPositions())
                .containsEntry("INFY", 0L);
    }

    @Test
    void shouldIgnoreDuplicateEventId() {
        positionService.process(new OrderEvent(
                "evt-0001",
                "RELIANCE",
                TransactionType.BUY,
                100
        ));

        positionService.process(new OrderEvent(
                "evt-0001",
                "RELIANCE",
                TransactionType.SELL,
                50
        ));

        assertThat(positionService.getPositions())
                .containsEntry("RELIANCE", 100L);
    }

    @Test
    void shouldCalculateNetPositionForMultipleEvents() {
        positionService.process(new OrderEvent(
                "evt-0001",
                "RELIANCE",
                TransactionType.BUY,
                100
        ));

        positionService.process(new OrderEvent(
                "evt-0002",
                "RELIANCE",
                TransactionType.SELL,
                30
        ));

        positionService.process(new OrderEvent(
                "evt-0003",
                "RELIANCE",
                TransactionType.BUY,
                20
        ));

        assertThat(positionService.getPositions())
                .containsEntry("RELIANCE", 90L);
    }
}