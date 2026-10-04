package model;

import repository.TicketRepository;

import java.time.LocalDateTime;

public class Ticket {
    private long id;
    private String title;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private LocalDateTime createAt;

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return  description; }
    public TicketStatus getStatus() { return status; }
    public TicketPriority getPriority() { return priority; }
    public LocalDateTime getCreateAt() { return createAt; }


    public Ticket(long id, String title, String description, TicketPriority priority)  {
        if (description != null && description.isEmpty()) {
            throw new IllegalArgumentException("Value must not be empty");
        }
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = TicketStatus.NEW;
        this.priority = priority;
        this.createAt = LocalDateTime.now();
    }

    public void startProcessing() {
        if ( status != TicketStatus.NEW ) {
//            System.out.println("Error: Your problem has solved or in progress");
//            return;
            throw new IllegalStateException("Error: Your problem has solved or in progress");
        }
        this.status = TicketStatus.IN_PROGRESS;
    }

    public void resolve() {
        if ( status != TicketStatus.IN_PROGRESS ) {
//            System.out.println("Error: solve problem can only progress in");
//            return;
            throw new IllegalStateException("Error: solve problem can only progress in");
        }
        this.status = TicketStatus.RESOLVED;
    }

    public void close() {
        if ( status != TicketStatus.RESOLVED ) {
//            System.out.println("Error: close problem need after solving");
//            return;
            throw new IllegalStateException("Error: close problem need after solving");
        }
        this.status = TicketStatus.CLOSED;
    }
// Добавил функцию отмены, если задача уже закрыта, то ее физически нельзя отменить,
// а в остальных случаях разреншаю как в бизнес процессах
    public void cancel() {
        if ( status == TicketStatus.CLOSED) {
//            System.out.println("Error: issue cannot canceled after close");
//            return;
            throw new IllegalStateException("Error: issue cannot canceled after close");
        }
        this.status = TicketStatus.CANCELED;
    }

    public String toString() {
        return String.format(
                "Ticket N%d | \"%s\" | status: %s | priority: %s | created: %s",
                this.id, this.title, this.status, this.priority, this.createAt
        );
    }

//    Если написать что-то другое отличное от "NEW" и
//    если мы не предусомотрели это то любой новый появившийся обЪект
//    с произвольными данными в статусе приведет к
//    потере этого обьекта и станет не валидным

//    ticket.setStatus("CLOSED") это утечка внутренного элемента в наружу
//    ticket.close() говорит, что происходит с заявкой в термиах предметной обалсти
//    "Эаявка закрывается". Это намерение а не механические воздействие
}
