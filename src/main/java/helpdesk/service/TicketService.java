package service;

import model.Administrator;
import model.SupportAgent;
import model.Ticket;
import model.User;

import java.util.Formatter;

public class TicketService {
    private final NotificationService notificationService;

    public  TicketService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    // я поставил 1==1 только потому что я не придумал ничего лучше чем
    // просто оставить так, без базы данных это не возможно пока реализоват

    public void getTicket(Ticket ticket) {
        notificationService.send("Заявка N" + ticket.getId() +": " + ticket.getDescription() + " | " + ticket.getStatus());
    }

    public void startTicket(User user, Ticket ticket) {
        if (!(user instanceof SupportAgent)) {
            throw new SecurityException("Only Support agent has access to this");
        }
        ticket.startProcessing();
        notificationService.send(ticket.toString());
    }

    public void progressIn(User user, Ticket ticket) {
        if (!(user instanceof SupportAgent)) {
            throw new SecurityException("Only Support agent has access to this");
        }
        ticket.resolve();
        notificationService.send(ticket.toString());
    }

    public void closeIt(User user, Ticket ticket) {
        if (!(user instanceof SupportAgent)) {
            throw new SecurityException("Only Support agent has access to this");
        }
        ticket.close();
        notificationService.send(ticket.toString());
    }

    public void cancelIt(User user, Ticket ticket) {
        if (!(user instanceof SupportAgent)) {
            throw new SecurityException("Only Support agent has access to this");
        }
        ticket.cancel();
        notificationService.send(ticket.toString());
    }
}
