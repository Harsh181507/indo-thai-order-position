package com.pm.orderupdateservice.service;

import com.pm.orderupdateservice.config.OrderServiceProperties;
import com.pm.orderupdateservice.csv.CsvOrderReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class OrderFileProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(OrderFileProcessor.class);

    private final CsvOrderReader csvOrderReader;
    private final OrderProcessingService orderProcessingService;
    private final OrderServiceProperties properties;
    private final ProcessingStatistics statistics;

    public OrderFileProcessor(
            CsvOrderReader csvOrderReader,
            OrderProcessingService orderProcessingService,
            OrderServiceProperties properties,
            ProcessingStatistics statistics
    ) {
        this.csvOrderReader = csvOrderReader;
        this.orderProcessingService = orderProcessingService;
        this.properties = properties;
        this.statistics = statistics;
    }

    public void process() {

        Path inputFile = Path.of(properties.inputFile());

        log.info(
                "Starting order file processing: file={}",
                inputFile
        );

        try (Reader reader = Files.newBufferedReader(inputFile)) {

            csvOrderReader.read(
                    reader,
                    orderProcessingService::process
            );

            log.info(
                    "Order file processing complete: file={}, total={}, successful={}, invalid={}, duplicate={}, failed={}",
                    inputFile,
                    statistics.totalEvents(),
                    statistics.successfulEvents(),
                    statistics.invalidEvents(),
                    statistics.duplicateEvents(),
                    statistics.failedEvents()
            );

        } catch (IOException exception) {

            log.error(
                    "Failed to read order file: file={}, reason={}",
                    inputFile,
                    exception.getMessage(),
                    exception
            );

            throw new IllegalStateException(
                    "Failed to process order file: " + inputFile,
                    exception
            );
        }
    }
}