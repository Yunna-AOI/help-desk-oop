package repository;

import model.Ticket;

import java.util.List;

public interface TagRepository {
    void addTag(long ticketId, String tagName);

    List<Ticket> findByTag(String tagName);

    List<String> findTagsByTicketId(long ticketId);
}
