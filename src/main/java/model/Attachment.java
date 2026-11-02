package model;

import java.time.LocalDateTime;

public class Attachment {
    private Long id;
    private Long ticketId;
    private Long uploadedById;
    private String originalName;
    private String storagePath;
    private String contentType;
    private long sizeBytes;
    private LocalDateTime uploadedAt;

    public Attachment() {
    }

    public Attachment(Long ticketId, Long uploadedById, String originalName,
                      String storagePath, String contentType, long sizeBytes) {
        this.ticketId = ticketId;
        this.uploadedById = uploadedById;
        this.originalName = originalName;
        this.storagePath = storagePath;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
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

    public Long getUploadedById() {
        return uploadedById;
    }

    public void setUploadedById(Long uploadedById) {
        this.uploadedById = uploadedById;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    @Override
    public String toString() {
        return "Attachment{id=" + id + ", name='" + originalName + "', size=" + sizeBytes + "}";
    }
}
