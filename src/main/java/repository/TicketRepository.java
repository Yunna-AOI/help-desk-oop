package repository;

import model.Ticket;
import model.TicketDetails;
import model.TicketPriority;
import model.TicketStatus;

import java.util.List;
import java.util.Optional;

public interface TicketRepository {
    Ticket save(Ticket ticket);

    Optional<Ticket> findById(long id);

    List<Ticket> findAll();

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByStatusAndPriority(TicketStatus status, TicketPriority priority);

    List<Ticket> findByCustomerId(long customerId);

    List<Ticket> findPage(int page, int size);

    List<Ticket> search(String text);

    List<Ticket> findOverdue();

    List<TicketDetails> findDetails();

    Optional<TicketDetails> findDetailsById(long id);

    void update(Ticket ticket);

    boolean deleteById(long id);

    boolean changeStatus(long ticketId, TicketStatus newStatus, long changedByUserId);

    boolean assign(long ticketId, long agentId, long changedByUserId);
}
