package model;

import java.time.LocalDateTime;

public class TicketChanges {
    private final TicketStatus oldStatus;
    private final TicketStatus newStatus;
    private final LocalDateTime changedAt;

    public TicketChanges(TicketStatus oldStatus, TicketStatus newStatus) {
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedAt = LocalDateTime.now();
    }

    public TicketStatus getNewStatus() { return newStatus; }
    public TicketStatus getOldStatus() { return oldStatus; }
    public LocalDateTime getChangedAt() { return changedAt; }
}
