package repository;

import model.SlaPolicy;
import model.TicketPriority;

import java.util.List;
import java.util.Optional;

public interface SlaPolicyRepository {
    Optional<SlaPolicy> findByPriority(TicketPriority priority);

    List<SlaPolicy> findAll();
}
