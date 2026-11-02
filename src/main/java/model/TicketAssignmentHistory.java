package model;

import java.time.LocalDateTime;

public class TicketAssignmentHistory {
    private Long id;
    private Long ticketId;
    private Long oldAssigneeId;
    private String oldAssigneeName;
    private Long newAssigneeId;
    private String newAssigneeName;
    private Long changedById;
    private String changedByName;
    private LocalDateTime changedAt;

    public TicketAssignmentHistory() {
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

    public Long getOldAssigneeId() {
        return oldAssigneeId;
    }

    public void setOldAssigneeId(Long oldAssigneeId) {
        this.oldAssigneeId = oldAssigneeId;
    }

    public String getOldAssigneeName() {
        return oldAssigneeName;
    }

    public void setOldAssigneeName(String oldAssigneeName) {
        this.oldAssigneeName = oldAssigneeName;
    }

    public Long getNewAssigneeId() {
        return newAssigneeId;
    }

    public void setNewAssigneeId(Long newAssigneeId) {
        this.newAssigneeId = newAssigneeId;
    }

    public String getNewAssigneeName() {
        return newAssigneeName;
    }

    public void setNewAssigneeName(String newAssigneeName) {
        this.newAssigneeName = newAssigneeName;
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
                "%s -> %s | изменил: %s | %s",
                oldAssigneeName == null ? "не назначен" : oldAssigneeName,
                newAssigneeName == null ? "не назначен" : newAssigneeName,
                changedByName == null ? "система" : changedByName,
                changedAt
        );
    }
}
