package com.pm.positionservice.controller;

import com.pm.positionservice.model.OrderEvent;
import com.pm.positionservice.model.TransactionType;
import com.pm.positionservice.service.PositionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PositionController.class)
class PositionControllerConcurrencyTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PositionService positionService;

    @Test
    void shouldReturnCurrentPositionsDuringConcurrentRequests() throws Exception {
        when(positionService.getPositions())
                .thenReturn(Map.of("RELIANCE", 1000L));

        int requestCount = 100;

        try (ExecutorService executorService = Executors.newFixedThreadPool(10)) {
            for (int i = 0; i < requestCount; i++) {
                executorService.submit(() -> {
                    try {
                        mockMvc.perform(get("/position")
                                        .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.RELIANCE").value(1000));
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }
                });
            }

            executorService.shutdown();

            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Concurrent requests did not complete");
            }
        }
    }
}