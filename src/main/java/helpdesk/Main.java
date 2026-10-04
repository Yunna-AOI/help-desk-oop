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
        users.add(new Customer(1, "Анна", "anna@mail.ru", false));
        users.add(new SupportAgent(2, "Сергей", "sergey@helpdesk.ru", true));
        users.add(new Administrator(3, "Олег", "admin@helpdesk.ru", true));

        TicketService ticketService = new TicketService(notificationService);

        ticketService.progressIn(ticket);
        ticketService.closeIt(ticket);
    }
}