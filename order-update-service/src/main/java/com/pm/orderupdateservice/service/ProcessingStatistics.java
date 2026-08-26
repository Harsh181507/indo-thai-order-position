package com.pm.orderupdateservice.service;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

@Component
public class ProcessingStatistics {

    private final AtomicLong totalEvents = new AtomicLong();
    private final AtomicLong successfulEvents = new AtomicLong();
    private final AtomicLong invalidEvents = new AtomicLong();
    private final AtomicLong duplicateEvents = new AtomicLong();
    private final AtomicLong failedEvents = new AtomicLong();

    public void recordTotal() {
        totalEvents.incrementAndGet();
    }

    public void recordSuccess() {
        successfulEvents.incrementAndGet();
    }

    public void recordInvalid() {
        invalidEvents.incrementAndGet();
    }

    public void recordDuplicate() {
        duplicateEvents.incrementAndGet();
    }

    public void recordFailure() {
        failedEvents.incrementAndGet();
    }

    public long totalEvents() {
        return totalEvents.get();
    }

    public long successfulEvents() {
        return successfulEvents.get();
    }

    public long invalidEvents() {
        return invalidEvents.get();
    }

    public long duplicateEvents() {
        return duplicateEvents.get();
    }

    public long failedEvents() {
        return failedEvents.get();
    }
}