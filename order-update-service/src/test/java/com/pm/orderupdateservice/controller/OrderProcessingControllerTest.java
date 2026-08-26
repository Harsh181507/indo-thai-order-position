package com.pm.orderupdateservice.controller;

import com.pm.orderupdateservice.service.OrderFileProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderProcessingController.class)
class OrderProcessingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderFileProcessor orderFileProcessor;

    @Test
    void shouldProcessOrders() throws Exception {
        mockMvc.perform(post("/orders/process"))
                .andExpect(status().isAccepted());

        verify(orderFileProcessor).process();
    }
}