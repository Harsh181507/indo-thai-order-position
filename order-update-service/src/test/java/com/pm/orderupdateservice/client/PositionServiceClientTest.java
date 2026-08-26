package com.pm.orderupdateservice.client;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PositionServiceClientTest {

    @Test
    void shouldCreateClientWithConfiguredBaseUrl() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        RestClient restClient = mock(RestClient.class);

        when(builder.baseUrl("http://localhost:8081")).thenReturn(builder);
        when(builder.build()).thenReturn(restClient);

        new PositionServiceClient(
                builder,
                "http://localhost:8081"
        );

        verify(builder).baseUrl("http://localhost:8081");
        verify(builder).build();
    }
}