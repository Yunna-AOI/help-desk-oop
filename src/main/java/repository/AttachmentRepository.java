package repository;

import model.Attachment;

import java.util.List;

public interface AttachmentRepository {
    Attachment save(Attachment attachment);

    List<Attachment> findByTicketId(long ticketId);
}
