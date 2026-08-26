package com.pm.positionservice.integration;

import com.pm.positionservice.model.OrderEvent;
import com.pm.positionservice.model.TransactionType;
import com.pm.positionservice.service.PositionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PositionIntegrationTest {

    @Autowired
    private PositionService positionService;

    @Test
    void shouldProcessBuyAndSellEvents() {
        positionService.clear();

        positionService.process(new OrderEvent(
                "evt-int-0001",
                "RELIANCE",
                TransactionType.BUY,
                100
        ));

        positionService.process(new OrderEvent(
                "evt-int-0002",
                "RELIANCE",
                TransactionType.SELL,
                40
        ));

        assertThat(positionService.getPositions())
                .containsEntry("RELIANCE", 60L);
    }

    @Test
    void shouldIgnoreDuplicateEvent() {
        positionService.clear();

        OrderEvent event = new OrderEvent(
                "evt-int-0003",
                "TCS",
                TransactionType.BUY,
                100
        );

        positionService.process(event);
        positionService.process(event);

        assertThat(positionService.getPositions())
                .containsEntry("TCS", 100L);
    }
}