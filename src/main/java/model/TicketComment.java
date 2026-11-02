package model;

import java.time.LocalDateTime;

public class TicketComment {
    private Long id;
    private Long ticketId;
    private Long authorId;
    private String authorName;
    private UserRole authorRole;
    private String text;
    private boolean internal;
    private LocalDateTime createdAt;

    public TicketComment() {
    }

    public TicketComment(Long ticketId, Long authorId, String text, boolean internal) {
        this.ticketId = ticketId;
        this.authorId = authorId;
        this.text = text;
        this.internal = internal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public UserRole getAuthorRole() {
        return authorRole;
    }

    public void setAuthorRole(UserRole authorRole) {
        this.authorRole = authorRole;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format(
                "[%s] %s (%s)%s: %s",
                createdAt, authorName, authorRole, internal ? " [internal]" : "", text
        );
    }
}
