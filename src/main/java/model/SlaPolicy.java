package model;

import java.time.Duration;

public class SlaPolicy {
    private Long id;
    private String name;
    private TicketPriority priority;
    private int responseMinutes;
    private int resolveMinutes;

    public SlaPolicy() {
    }

    public SlaPolicy(Long id, String name, TicketPriority priority,
                     int responseMinutes, int resolveMinutes) {
        this.id = id;
        this.name = name;
        this.priority = priority;
        this.responseMinutes = responseMinutes;
        this.resolveMinutes = resolveMinutes;
    }

    public SlaPolicy(TicketPriority priority, Duration resolutionTime) {
        this.priority = priority;
        this.resolveMinutes = (int) resolutionTime.toMinutes();
        this.responseMinutes = this.resolveMinutes;
        this.name = priority.name();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public int getResponseMinutes() {
        return responseMinutes;
    }

    public void setResponseMinutes(int responseMinutes) {
        this.responseMinutes = responseMinutes;
    }

    public int getResolveMinutes() {
        return resolveMinutes;
    }

    public void setResolveMinutes(int resolveMinutes) {
        this.resolveMinutes = resolveMinutes;
    }

    public Duration getResolutionTime() {
        return Duration.ofMinutes(resolveMinutes);
    }

    @Override
    public String toString() {
        return "SlaPolicy{priority=" + priority + ", response=" + responseMinutes
                + "m, resolve=" + resolveMinutes + "m}";
    }
}
