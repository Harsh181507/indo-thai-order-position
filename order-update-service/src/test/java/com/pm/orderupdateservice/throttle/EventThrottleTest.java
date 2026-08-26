package com.pm.orderupdateservice.throttle;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventThrottleTest {

    @Test
    void shouldRejectNonPositiveRate() {
        assertThatThrownBy(() -> new EventThrottle(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("maxEventsPerSecond must be positive");
    }

    @Test
    void shouldRejectNegativeRate() {
        assertThatThrownBy(() -> new EventThrottle(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("maxEventsPerSecond must be positive");
    }
}