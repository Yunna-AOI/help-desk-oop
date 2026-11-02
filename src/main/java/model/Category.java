package model;

public class Category {
    private Long id;
    private String name;
    private String description;
    private TicketPriority defaultPriority;
    private boolean active;

    public Category() {
    }

    public Category(Long id, String name, String description, TicketPriority defaultPriority, boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.defaultPriority = defaultPriority;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TicketPriority getDefaultPriority() {
        return defaultPriority;
    }

    public void setDefaultPriority(TicketPriority defaultPriority) {
        this.defaultPriority = defaultPriority;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Category{id=" + id + ", name='" + name + "', defaultPriority=" + defaultPriority
                + ", active=" + active + "}";
    }
}
