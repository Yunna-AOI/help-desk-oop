package model;

public enum TicketStatus {
    NEW,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    CANCELLED;

    public boolean canTransitionTo(TicketStatus next) {
        if (next == null) {
            return false;
        }
        return switch (this) {
            case NEW -> next == IN_PROGRESS || next == CANCELLED;
            case IN_PROGRESS -> next == RESOLVED || next == CANCELLED;
            case RESOLVED -> next == CLOSED || next == IN_PROGRESS;
            case CLOSED, CANCELLED -> false;
        };
    }
}
