package repository.jdbc;

import config.DatabaseConfig;
import model.Attachment;
import repository.AttachmentRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcAttachmentRepository implements AttachmentRepository {

    @Override
    public Attachment save(Attachment attachment) {
        String sql = """
                INSERT INTO attachments(
                    ticket_id, uploaded_by_id, original_name,
                    storage_path, content_type, size_bytes
                )
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id, uploaded_at
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, attachment.getTicketId());
            ps.setLong(2, attachment.getUploadedById());
            ps.setString(3, attachment.getOriginalName());
            ps.setString(4, attachment.getStoragePath());
            ps.setString(5, attachment.getContentType());
            ps.setLong(6, attachment.getSizeBytes());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    attachment.setId(rs.getLong("id"));
                    attachment.setUploadedAt(rs.getTimestamp("uploaded_at").toLocalDateTime());
                }
            }
            return attachment;

        } catch (SQLException e) {
            throw new RuntimeException("Не удалось сохранить вложение", e);
        }
    }

    @Override
    public List<Attachment> findByTicketId(long ticketId) {
        String sql = "SELECT * FROM attachments WHERE ticket_id = ? ORDER BY id";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, ticketId);

            try (ResultSet rs = ps.executeQuery()) {
                List<Attachment> result = new ArrayList<>();
                while (rs.next()) {
                    Attachment attachment = new Attachment();
                    attachment.setId(rs.getLong("id"));
                    attachment.setTicketId(rs.getLong("ticket_id"));
                    attachment.setUploadedById(rs.getLong("uploaded_by_id"));
                    attachment.setOriginalName(rs.getString("original_name"));
                    attachment.setStoragePath(rs.getString("storage_path"));
                    attachment.setContentType(rs.getString("content_type"));
                    attachment.setSizeBytes(rs.getLong("size_bytes"));
                    attachment.setUploadedAt(rs.getTimestamp("uploaded_at").toLocalDateTime());
                    result.add(attachment);
                }
                return result;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения вложений", e);
        }
    }
}
