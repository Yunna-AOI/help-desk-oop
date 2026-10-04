package repository;

import model.Ticket;
import model.TicketPriority;
import model.TicketStatus;

import java.util.*;
import java.util.stream.Collectors;

public class TicketRepository {
    private  final Map<Long, Ticket> tickets = new HashMap<>();

    public void add(Ticket ticket) {
        tickets.put(ticket.getId(), ticket);
    }

    public List<Ticket> findAll() {
        return new ArrayList<>(tickets.values());
    }

    public Optional<Ticket> findById(long id) {
        Ticket ticket = tickets.get(id);
        Optional<Ticket> opt = Optional.ofNullable(ticket);
        return opt;
    }

    public List<Ticket> findByInterface(TicketStatus status, TicketPriority priority) {
        return findAll().stream()
                .filter(ticket->status == null || ticket.getStatus() == status )
                .filter(ticket->priority == null || ticket.getPriority() == priority)
                .collect(Collectors.toList());
    }

    // I don't check this block, but I believe he can work :)
}
