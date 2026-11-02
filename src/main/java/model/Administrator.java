package model;

public class Administrator extends User {
    public Administrator() {
        super();
    }

    public Administrator(Long id, String name, String email) {
        super(id, name, email, UserRole.ADMIN, null);
    }

    public Administrator(Long id, String name, String email, Long departmentId) {
        super(id, name, email, UserRole.ADMIN, departmentId);
    }

    @Override
    public void performAction() {
        System.out.println(getName() + " администрирует систему");
    }
}
