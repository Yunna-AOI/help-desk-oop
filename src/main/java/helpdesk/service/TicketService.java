package service;

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

    public void startTicket(Ticket ticket) {
        ticket.startProcessing();
        notificationService.send(ticket.toString());
    }

    public void progressIn(Ticket ticket) {
        ticket.resolve();
        notificationService.send(ticket.toString());
    }

    public void closeIt(Ticket ticket) {
        ticket.close();
        notificationService.send(ticket.toString());
    }

    public void cancelIt(Ticket ticket) {
        ticket.cancel();
        notificationService.send(ticket.toString());
    }
}
