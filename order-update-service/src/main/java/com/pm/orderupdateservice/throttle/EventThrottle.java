package com.pm.orderupdateservice.throttle;

import java.util.concurrent.locks.LockSupport;

public class EventThrottle {

    private final long intervalNanos;
    private long nextAvailableTime;

    public EventThrottle(int maxEventsPerSecond) {
        if (maxEventsPerSecond <= 0) {
            throw new IllegalArgumentException("maxEventsPerSecond must be positive");
        }

        this.intervalNanos = 1_000_000_000L / maxEventsPerSecond;
        this.nextAvailableTime = System.nanoTime();
    }

    public synchronized void acquire() {
        while (true) {
            long currentTime = System.nanoTime();

            if (currentTime >= nextAvailableTime) {
                nextAvailableTime = currentTime + intervalNanos;
                return;
            }

            long waitNanos = nextAvailableTime - currentTime;
            LockSupport.parkNanos(waitNanos);

            if (Thread.currentThread().isInterrupted()) {
                throw new IllegalStateException("Event throttle interrupted");
            }
        }
    }
}