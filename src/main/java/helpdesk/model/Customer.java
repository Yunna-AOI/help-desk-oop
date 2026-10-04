package model;

public class Customer extends User {
    public Customer(long id, String name, String mail, Boolean boolAdmin) {

        super(id, name, mail, boolAdmin);
    }

    @Override
    public void performAction() {
        System.out.println(getName() + " create ticket");
    }
}
