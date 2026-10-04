package model;

import java.util.ArrayList;
import java.util.List;

public class SupportAgent extends User{
    private List<Ticket> assignedTickets = new ArrayList<>();

    public SupportAgent(long id, String name, String mail) {
        super(id, name, mail);
    }

    @Override
    public void performAction() {

    }

    @Override
    public void assignTicket(Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket cannot be null");
        }
        assignedTickets.add(ticket);
    }
}
