import config.DatabaseConfig;
import model.Category;
import model.Ticket;
import model.TicketComment;
import model.TicketPriority;
import model.TicketStatus;
import model.User;
import repository.jdbc.JdbcAssignmentHistoryRepository;
import repository.jdbc.JdbcAttachmentRepository;
import repository.jdbc.JdbcAuditRepository;
import repository.jdbc.JdbcCategoryRepository;
import repository.jdbc.JdbcCommentRepository;
import repository.jdbc.JdbcDepartmentRepository;
import repository.jdbc.JdbcReportRepository;
import repository.jdbc.JdbcSlaPolicyRepository;
import repository.jdbc.JdbcStatusHistoryRepository;
import repository.jdbc.JdbcTagRepository;
import repository.jdbc.JdbcTicketRepository;
import repository.jdbc.JdbcUserRepository;
import service.ConsoleNotificationService;
import service.TicketService;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Main {

    public static void main(String[] args) {
        System.out.println(" === HELP DESK: PostgreSQL === ");

        try {
            checkConnection();
            TicketService service = buildService();
            runScenario(service);
        } catch (Exception e) {
            System.err.println("Ошибка выполнения: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void checkConnection() throws Exception {
        try (Connection connection = DatabaseConfig.getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT version()")) {

            System.out.println("URL: " + DatabaseConfig.getUrl());
            System.out.println("Пользователь: " + DatabaseConfig.getUser());
            if (rs.next()) {
                System.out.println("Сервер: " + rs.getString(1));
            }
            System.out.println("AutoCommit = " + connection.getAutoCommit());
        }
    }

    private static TicketService buildService() {
        return new TicketService(
                new JdbcTicketRepository(),
                new JdbcUserRepository(),
                new JdbcCategoryRepository(),
                new JdbcDepartmentRepository(),
                new JdbcCommentRepository(),
                new JdbcStatusHistoryRepository(),
                new JdbcAssignmentHistoryRepository(),
                new JdbcAttachmentRepository(),
                new JdbcSlaPolicyRepository(),
                new JdbcTagRepository(),
                new JdbcReportRepository(),
                new JdbcAuditRepository(),
                new ConsoleNotificationService()
        );
    }

    private static void runScenario(TicketService service) {
        User customer = service.findUserByEmail("anna@example.org")
                .orElseThrow(() -> new IllegalStateException("Клиент не найден"));
        User agent = service.findUserByEmail("ivan.support@example.org")
                .orElseThrow(() -> new IllegalStateException("Сотрудник не найден"));
        User admin = service.findUserByEmail("admin@example.org")
                .orElseThrow(() -> new IllegalStateException("Администратор не найден"));

        section("1. Клиент");
        System.out.println(customer);

        section("2. Категории");
        Category category = null;
        for (Category item : service.findAllCategories()) {
            System.out.println(item);
            if (item.getName().equals("Оборудование")) {
                category = item;
            }
        }

        if (category == null) {
            throw new IllegalStateException("Категория 'Оборудование' не найдена");
        }

        section("3. Создание заявки");
        Ticket ticket = service.createTicket(
                "Принтер не печатает",
                "После обновления драйвера принтер перестал печатать документы",
                TicketPriority.HIGH,
                customer.getId(),
                category.getId()
        );
        System.out.println("Создана заявка с id = " + ticket.getId());
        System.out.println(ticket);

        section("4. Чтение заявки через findById");
        Ticket loaded = service.findTicket(ticket.getId())
                .orElseThrow(() -> new IllegalStateException("Заявка не найдена"));
        System.out.println(loaded);

        section("5. Назначение сотрудника");
        service.assignTicket(ticket.getId(), agent.getId(), admin.getId());
        System.out.println("Заявка назначена: " + agent.getName());

        section("6. Клиентский комментарий");
        TicketComment clientComment = service.addComment(
                ticket.getId(),
                customer.getId(),
                "Пожалуйста, помогите быстрее, документы нужны к встрече",
                false
        );
        System.out.println(clientComment);

        section("7. IN_PROGRESS -> RESOLVED");
        service.changeStatus(ticket.getId(), TicketStatus.RESOLVED, agent.getId());

        section("8. Внутренний комментарий сотрудника");
        TicketComment internalComment = service.addComment(
                ticket.getId(),
                agent.getId(),
                "Переустановлен драйвер, выполнена тестовая печать",
                true
        );
        System.out.println(internalComment);

        section("9. RESOLVED -> CLOSED");
        service.changeStatus(ticket.getId(), TicketStatus.CLOSED, agent.getId());

        section("10. История статусов");
        service.getStatusHistory(ticket.getId()).forEach(System.out::println);

        section("11. История назначений");
        service.getAssignmentHistory(ticket.getId()).forEach(System.out::println);

        section("12. Комментарии заявки");
        service.getComments(ticket.getId()).forEach(System.out::println);

        section("13. Только клиентские комментарии");
        service.getExternalComments(ticket.getId()).forEach(System.out::println);

        section("14. JOIN-карточка заявки");
        service.printTicketCard(ticket.getId());

        section("15. Теги");
        service.addTag(ticket.getId(), "оборудование");
        service.addTag(ticket.getId(), "срочно");
        System.out.println("Теги заявки: " + service.getTags(ticket.getId()));
        System.out.println("Заявки с тегом 'срочно': " + service.findTicketsByTag("срочно").size());

        section("16. Вложение");
        service.addAttachment(
                ticket.getId(),
                agent.getId(),
                "driver-report.txt",
                "storage/" + ticket.getId() + "/driver-report.txt",
                "text/plain",
                2048
        );
        service.getAttachments(ticket.getId()).forEach(System.out::println);

        section("17. Отчёты");
        System.out.println("По статусам: " + service.reportByStatus());
        System.out.println("По категориям: " + service.reportByCategory());
        service.reportAgentStats().forEach(System.out::println);
        System.out.println("Среднее время решения: " + service.reportAverageResolution());

        section("18. Просроченные заявки");
        service.findOverdueTickets().forEach(System.out::println);

        section("19. Пагинация и поиск");
        System.out.println("Страница 1, размер 5:");
        service.findTicketPage(1, 5).forEach(System.out::println);
        System.out.println("Поиск 'принтер':");
        service.searchTickets("принтер").forEach(System.out::println);

        section("20. Audit log");
        service.recentAudit(10).forEach(System.out::println);

        section("21. Все заявки в базе");
        service.findAllTickets().forEach(System.out::println);

        System.out.println();
        System.out.println("Заявка N" + ticket.getId()
                + " сохранена в PostgreSQL. Перезапустите программу — она останется в базе.");
    }

    private static void section(String title) {
        System.out.println();
        System.out.println(" --- " + title + " ---");
    }
}
