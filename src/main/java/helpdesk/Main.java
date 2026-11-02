import model.*;
import service.ConsoleNotificationService;
import service.NotificationService;
import service.TicketService;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println(" === HELP DESK === ");
        NotificationService notificationService = new ConsoleNotificationService();

        Ticket ticket = new Ticket(
                1,
                "Internet not working",
                "After reboot pc connection lost",
                TicketPriority.HIGH
        );

        List<User> users = new ArrayList<>();
        users.add(new Customer(1, "Анна", "anna@mail.ru"));
        users.add(new SupportAgent(2, "Сергей", "sergey@helpdesk.ru"));
        users.add(new Administrator(3, "Олег", "admin@helpdesk.ru"));

        TicketService ticketService = new TicketService(notificationService);

        ticketService.startTicket(users.get(1), ticket);
        ticketService.progressIn(users.get(1), ticket);
        ticketService.closeIt(users.get(1), ticket);

        for (TicketChanges tch : ticket.getStatusHistory()) {
            System.out.println(String.format("Old change: %s \nNew status: %s \nChange at: %s", tch.getOldStatus(), tch.getNewStatus(), tch.getChangedAt()));
        }
    }
}