package com.pm.positionservice.controller;

import com.pm.positionservice.model.OrderEvent;
import com.pm.positionservice.service.PositionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping
public class PositionController {

    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @PostMapping("/events")
    public ResponseEntity<Void> processEvent(@RequestBody OrderEvent event) {
        positionService.process(event);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/position")
    public ResponseEntity<Map<String, Long>> getPositions() {
        return ResponseEntity.ok(positionService.getPositions());
    }
}