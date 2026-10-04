package model;

public class Administrator extends User {
    public Administrator(long id, String name, String mail) {
        super(id, name, mail);
    }

    @Override
    public void performAction() {
        System.out.println(getName() + " create ticket");
    }

}
