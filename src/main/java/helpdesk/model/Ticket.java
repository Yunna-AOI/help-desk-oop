package model;

public class Ticket {
    private long id;
    private String title;
    private String description;
    private TicketStatus status;

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return  description; }
    public TicketStatus getStatus() { return status; }

    public Ticket(long id, String title, String description)  {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = TicketStatus.NEW;
    }

    public void startProcessing() {
        if ( status != TicketStatus.NEW ) {
            System.out.println("Error: Your problem has solved or in progress");
            return;
        }
        this.status = TicketStatus.IN_PROGRESS;
    }

    public void resolve() {
        if ( status != TicketStatus.IN_PROGRESS ) {
            System.out.println("Error: solve problem can only progress in");
            return;
        }
        this.status = TicketStatus.RESOLVED;
    }

    public void close() {
        if ( status != TicketStatus.RESOLVED ) {
            System.out.println("Error: close problem need after solving");
            return;
        }
        this.status = TicketStatus.CLOSED;
    }

//    Если написать что-то другое отличное от "NEW" и
//    если мы не предусомотрели это то любой новый появившийся обЪект
//    с произвольными данными в статусе приведет к
//    потере этого обьекта и станет не валидным

//    ticket.setStatus("CLOSED") это утечка внутренного элемента в наружу
//    ticket.close() говорит, что происходит с заявкой в термиах предметной обалсти
//    "Эаявка закрывается". Это намерение а не механические воздействие
}
