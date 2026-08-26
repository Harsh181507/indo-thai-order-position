package com.pm.orderupdateservice.client;

import com.pm.orderupdateservice.model.OrderEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
        ResponseEntity<Void> response = restClient
                .post()
                .uri("/events")
                .body(event)
                .retrieve()
                .toBodilessEntity();

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException(
                    "Position service returned status: " + response.getStatusCode()
            );
        }
    }
}