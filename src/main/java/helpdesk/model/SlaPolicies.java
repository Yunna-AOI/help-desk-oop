package model;

import java.time.Duration;

public class SlaPolicies {
    public static SlaPolicy forPolicy(TicketPriority priority) {
        return switch ( priority ) {
            case CRITICAL -> new SlaPolicy(priority, Duration.ofMinutes(30));
            case HIGH -> new SlaPolicy(priority, Duration.ofHours(2));
            case MEDIUM -> new SlaPolicy(priority, Duration.ofHours(8));
            case LOW -> new SlaPolicy(priority, Duration.ofHours(24));
        };
    }
}
