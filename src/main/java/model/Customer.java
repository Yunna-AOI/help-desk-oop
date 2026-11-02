package model;

public class Customer extends User {
    public Customer() {
        super();
    }

    public Customer(Long id, String name, String email) {
        super(id, name, email, UserRole.CUSTOMER, null);
    }

    public Customer(Long id, String name, String email, Long departmentId) {
        super(id, name, email, UserRole.CUSTOMER, departmentId);
    }

    @Override
    public void performAction() {
        System.out.println(getName() + " создаёт заявку");
    }
}
