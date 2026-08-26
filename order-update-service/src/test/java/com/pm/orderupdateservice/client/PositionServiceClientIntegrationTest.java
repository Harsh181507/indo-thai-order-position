package com.pm.orderupdateservice.client;

import com.pm.orderupdateservice.model.OrderEvent;
import com.pm.orderupdateservice.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PositionServiceClientIntegrationTest {

    @Autowired
    private PositionServiceClient positionServiceClient;

    @Test
    void shouldCreatePositionServiceClient() {
        assertThat(positionServiceClient).isNotNull();
    }
}