package com.pm.orderupdateservice.client;

import com.pm.orderupdateservice.model.OrderEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class PositionServiceClient {

    private final RestClient restClient;

    public PositionServiceClient(
            RestClient.Builder restClientBuilder,
            @Value("${position-service.base-url}") String baseUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    public void send(OrderEvent event) {
        try {
            restClient
                    .post()
                    .uri("/events")
                    .body(event)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new IllegalStateException(
                    "Failed to send event to position service: " + event.eventId(),
                    exception
            );
        }
    }
}