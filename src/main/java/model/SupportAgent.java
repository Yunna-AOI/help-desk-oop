package model;

import java.util.ArrayList;
import java.util.List;

public class SupportAgent extends User {
    private final List<Ticket> assignedTickets = new ArrayList<>();

    public SupportAgent() {
        super();
    }

    public SupportAgent(Long id, String name, String email) {
        super(id, name, email, UserRole.SUPPORT_AGENT, null);
    }

    public SupportAgent(Long id, String name, String email, Long departmentId) {
        super(id, name, email, UserRole.SUPPORT_AGENT, departmentId);
    }

    @Override
    public void performAction() {
        System.out.println(getName() + " обрабатывает заявки");
    }

    @Override
    public void assignTicket(Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket cannot be null");
        }
        assignedTickets.add(ticket);
    }

    public List<Ticket> getAssignedTickets() {
        return List.copyOf(assignedTickets);
    }
}
