package model;

public abstract class User {
    private long id;
    private final String name;
    private final String mail;
    private final Boolean admin;

    public User(long id, String name, String mail, Boolean boolAdmin) {
        this.id = id;
        this.name = name;
        this.mail = mail;
        this.admin = boolAdmin;
    }

    public void performAction() {
        System.out.println("User working with system");
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getMail() { return mail; }
    public Boolean getAdmin() {return admin; }

}
