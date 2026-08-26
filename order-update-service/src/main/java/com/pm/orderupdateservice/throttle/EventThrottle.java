package com.pm.orderupdateservice.throttle;

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
        long currentTime = System.nanoTime();

        if (currentTime < nextAvailableTime) {
            long waitNanos = nextAvailableTime - currentTime;

            try {
                long millis = waitNanos / 1_000_000L;
                int nanos = (int) (waitNanos % 1_000_000L);

                Thread.sleep(millis, nanos);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Event throttle interrupted", exception);
            }
        }

        nextAvailableTime = Math.max(
                nextAvailableTime + intervalNanos,
                System.nanoTime()
        );
    }
}