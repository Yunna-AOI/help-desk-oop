package model;

public abstract class User {
    private long id;
    private final String name;
    private final String mail;

    public User(long id, String name, String mail) {
        this.id = id;
        this.name = name;
        this.mail = mail;
    }

    public void performAction() {
        System.out.println("User working with system");
    }
    public void assignTicket(Ticket ticket) throws IllegalAccessException {throw new IllegalAccessException("Default user cannot use this interface");}

    public long getId() { return id; }
    public String getName() { return name; }
    public String getMail() { return mail; }

}
