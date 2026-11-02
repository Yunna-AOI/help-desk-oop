package model;

import java.time.LocalDateTime;

public class TicketStatusHistory {
    private Long id;
    private Long ticketId;
    private TicketStatus oldStatus;
    private TicketStatus newStatus;
    private Long changedById;
    private String changedByName;
    private LocalDateTime changedAt;

    public TicketStatusHistory() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public TicketStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(TicketStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public TicketStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(TicketStatus newStatus) {
        this.newStatus = newStatus;
    }

    public Long getChangedById() {
        return changedById;
    }

    public void setChangedById(Long changedById) {
        this.changedById = changedById;
    }

    public String getChangedByName() {
        return changedByName;
    }

    public void setChangedByName(String changedByName) {
        this.changedByName = changedByName;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    @Override
    public String toString() {
        return String.format(
                "%s -> %s | %s | %s",
                oldStatus == null ? "—" : oldStatus,
                newStatus,
                changedByName == null ? "система" : changedByName,
                changedAt
        );
    }
}
