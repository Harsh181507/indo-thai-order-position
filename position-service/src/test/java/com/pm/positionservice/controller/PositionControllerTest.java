package com.pm.positionservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.positionservice.model.OrderEvent;
import com.pm.positionservice.model.TransactionType;
import com.pm.positionservice.service.PositionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PositionController.class)
class PositionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PositionService positionService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldProcessEvent() throws Exception {
        OrderEvent event = new OrderEvent(
                "evt-0001",
                "RELIANCE",
                TransactionType.BUY,
                90
        );

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk());

        verify(positionService).process(event);
    }

    @Test
    void shouldReturnCurrentPositions() throws Exception {
        when(positionService.getPositions())
                .thenReturn(Map.of(
                        "RELIANCE", 90L,
                        "TCS", -75L
                ));

        mockMvc.perform(get("/position"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RELIANCE").value(90))
                .andExpect(jsonPath("$.TCS").value(-75));
    }

    @Test
    void shouldReturnZeroPosition() throws Exception {
        when(positionService.getPositions())
                .thenReturn(Map.of("INFY", 0L));

        mockMvc.perform(get("/position"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.INFY").value(0));
    }
}