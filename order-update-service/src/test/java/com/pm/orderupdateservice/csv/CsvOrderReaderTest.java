package com.pm.orderupdateservice.csv;

import com.pm.orderupdateservice.model.OrderEvent;
import com.pm.orderupdateservice.model.TransactionType;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CsvOrderReaderTest {

    @Test
    void shouldReadEventsInCsvOrder() {
        String csv = """
                event_id,symbol,transaction_type,quantity
                evt-0001,RELIANCE,BUY,90
                evt-0002,TCS,SELL,75
                """;

        CsvOrderReader reader = new CsvOrderReader();
        List<OrderEvent> events = new ArrayList<>();

        reader.read(new StringReader(csv), events::add);

        assertThat(events).containsExactly(
                new OrderEvent("evt-0001", "RELIANCE", TransactionType.BUY, 90),
                new OrderEvent("evt-0002", "TCS", TransactionType.SELL, 75)
        );
    }
}