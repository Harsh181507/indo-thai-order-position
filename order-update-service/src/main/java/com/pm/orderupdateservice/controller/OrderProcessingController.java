package com.pm.orderupdateservice.controller;

import com.pm.orderupdateservice.service.OrderFileProcessor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderProcessingController {

    private final OrderFileProcessor orderFileProcessor;

    public OrderProcessingController(OrderFileProcessor orderFileProcessor) {
        this.orderFileProcessor = orderFileProcessor;
    }

    @PostMapping("/process")
    public ResponseEntity<Void> processOrders() {
        orderFileProcessor.process();
        return ResponseEntity.accepted().build();
    }
}