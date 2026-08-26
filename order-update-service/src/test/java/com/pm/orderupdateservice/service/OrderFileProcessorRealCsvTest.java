package com.pm.orderupdateservice.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OrderFileProcessorRealCsvTest {

    @Autowired
    private OrderFileProcessor orderFileProcessor;

    @Test
    void shouldProcessActualAssignmentCsv() {
        orderFileProcessor.process();
    }
}