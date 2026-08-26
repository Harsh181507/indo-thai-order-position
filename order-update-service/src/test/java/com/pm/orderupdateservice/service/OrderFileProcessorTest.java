package com.pm.orderupdateservice.service;

import com.pm.orderupdateservice.config.OrderServiceProperties;
import com.pm.orderupdateservice.csv.CsvOrderReader;
import com.pm.orderupdateservice.model.OrderEvent;
import com.pm.orderupdateservice.model.TransactionType;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OrderFileProcessorTest {

    private final CsvOrderReader csvOrderReader = mock(CsvOrderReader.class);
    private final OrderProcessingService orderProcessingService = mock(OrderProcessingService.class);

    @Test
    void shouldProcessAllEventsFromFile() throws Exception {
        Path tempFile = Files.createTempFile("orders-", ".csv");

        try {
            List<OrderEvent> events = List.of(
                    new OrderEvent(
                            "evt-0001",
                            "RELIANCE",
                            TransactionType.BUY,
                            90
                    ),
                    new OrderEvent(
                            "evt-0002",
                            "TCS",
                            TransactionType.SELL,
                            75
                    )
            );

            OrderServiceProperties properties = new OrderServiceProperties(
                    tempFile.toString(),
                    new OrderServiceProperties.Publisher(50)
            );

            doAnswer(invocation -> {
                Consumer<OrderEvent> consumer = invocation.getArgument(1);
                events.forEach(consumer);
                return null;
            }).when(csvOrderReader).read(
                    any(Reader.class),
                    any(Consumer.class)
            );

            OrderFileProcessor processor = new OrderFileProcessor(
                    csvOrderReader,
                    orderProcessingService,
                    properties
            );

            processor.process();

            verify(orderProcessingService).process(events.get(0));
            verify(orderProcessingService).process(events.get(1));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}