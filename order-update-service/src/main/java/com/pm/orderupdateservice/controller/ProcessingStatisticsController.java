package com.pm.orderupdateservice.controller;

import com.pm.orderupdateservice.service.ProcessingStatistics;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/processing")
public class ProcessingStatisticsController {

    private final ProcessingStatistics statistics;

    public ProcessingStatisticsController(ProcessingStatistics statistics) {
        this.statistics = statistics;
    }

    @GetMapping("/statistics")
    public Map<String, Long> getStatistics() {
        return Map.of(
                "totalEvents", statistics.totalEvents(),
                "successfulEvents", statistics.successfulEvents(),
                "invalidEvents", statistics.invalidEvents(),
                "duplicateEvents", statistics.duplicateEvents(),
                "failedEvents", statistics.failedEvents()
        );
    }
}