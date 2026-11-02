package repository;

import model.TicketStatusHistory;

import java.util.List;

public interface StatusHistoryRepository {
    List<TicketStatusHistory> findByTicketId(long ticketId);
}
