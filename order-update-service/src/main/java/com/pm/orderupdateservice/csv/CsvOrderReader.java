package com.pm.orderupdateservice.csv;

import com.pm.orderupdateservice.model.OrderEvent;
import com.pm.orderupdateservice.model.TransactionType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.util.function.Consumer;

@Component
public class CsvOrderReader {

    public void read(Reader reader, Consumer<OrderEvent> eventConsumer) {
        try (CSVParser parser = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setTrim(true)
                .build()
                .parse(reader)) {

            for (CSVRecord record : parser) {
                eventConsumer.accept(toOrderEvent(record));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read CSV input", exception);
        }
    }

    private OrderEvent toOrderEvent(CSVRecord record) {
        return new OrderEvent(
                record.get("event_id"),
                record.get("symbol"),
                parseTransactionType(record.get("transaction_type")),
                parseQuantity(record.get("quantity"))
        );
    }

    private TransactionType parseTransactionType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return TransactionType.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private Integer parseQuantity(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}