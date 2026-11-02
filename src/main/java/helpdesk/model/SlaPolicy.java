package model;

import java.time.Duration;

public class SlaPolicy {
    private final TicketPriority priority;
    private final Duration resolutionTime;

    public SlaPolicy(TicketPriority priority, Duration resolutionTime) {
        this.priority = priority;
        this.resolutionTime = resolutionTime;
    }

    public Duration getResolutionTime() { return resolutionTime; }
}
