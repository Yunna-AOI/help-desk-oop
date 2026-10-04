package model;

public class Customer extends User {
    public Customer(long id, String name, String mail) {

        super(id, name, mail);
    }

    @Override
    public void performAction() {
        System.out.println(getName() + " create ticket");
    }
}
