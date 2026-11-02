package repository;

import model.TicketAssignmentHistory;

import java.util.List;

public interface AssignmentHistoryRepository {
    List<TicketAssignmentHistory> findByTicketId(long ticketId);
}
