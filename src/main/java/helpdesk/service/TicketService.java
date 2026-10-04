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
        notificationService.send("Заявка N" + ticket.getId() + " принята в работу\nСтатус " + ticket.getStatus());
    }

    public void progressIn(Ticket ticket) {
        ticket.resolve();
        notificationService.send("Заявка N" + ticket.getId() + " найдено решение\nСтатус " + ticket.getStatus());
    }

    public void closeIt(Ticket ticket) {
        ticket.close();
        notificationService.send("Заявка N" + ticket.getId() + " ззыкрыта\nСтатус " + ticket.getStatus());
    }
}
