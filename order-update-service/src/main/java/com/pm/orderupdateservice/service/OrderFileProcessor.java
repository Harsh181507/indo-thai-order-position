package com.pm.orderupdateservice.service;

import com.pm.orderupdateservice.config.OrderServiceProperties;
import com.pm.orderupdateservice.csv.CsvOrderReader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class OrderFileProcessor {

    private final CsvOrderReader csvOrderReader;
    private final OrderProcessingService orderProcessingService;
    private final OrderServiceProperties properties;

    public OrderFileProcessor(
            CsvOrderReader csvOrderReader,
            OrderProcessingService orderProcessingService,
            OrderServiceProperties properties
    ) {
        this.csvOrderReader = csvOrderReader;
        this.orderProcessingService = orderProcessingService;
        this.properties = properties;
    }

    public void process() {
        Path inputFile = Path.of(properties.inputFile());

        try (Reader reader = Files.newBufferedReader(inputFile)) {
            csvOrderReader.read(reader, orderProcessingService::process);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to process order file: " + inputFile,
                    exception
            );
        }
    }
}