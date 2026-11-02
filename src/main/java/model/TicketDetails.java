package model;

import java.time.LocalDateTime;

public class TicketDetails {
    private Long id;
    private String title;
    private TicketStatus status;
    private TicketPriority priority;
    private String categoryName;
    private String customerName;
    private String assigneeName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime dueAt;

    public TicketDetails() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(LocalDateTime dueAt) {
        this.dueAt = dueAt;
    }

    @Override
    public String toString() {
        return String.format(
                "Заявка N%d%n  Тема: %s%n  Статус: %s | Приоритет: %s%n  Клиент: %s%n  Исполнитель: %s%n  Категория: %s%n  Создана: %s%n  Обновлена: %s%n  Дедлайн: %s",
                id, title, status, priority, customerName,
                assigneeName == null ? "не назначен" : assigneeName,
                categoryName == null ? "без категории" : categoryName,
                createdAt, updatedAt, dueAt
        );
    }
}
