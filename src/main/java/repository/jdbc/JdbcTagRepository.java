package repository.jdbc;

import config.DatabaseConfig;
import model.Ticket;
import model.TicketPriority;
import model.TicketStatus;
import repository.TagRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class JdbcTagRepository implements TagRepository {

    @Override
    public void addTag(long ticketId, String tagName) {
        String tagSql = """
                INSERT INTO tags(name) VALUES (?)
                ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
                RETURNING id
                """;
        String linkSql = """
                INSERT INTO ticket_tags(ticket_id, tag_id) VALUES (?, ?)
                ON CONFLICT DO NOTHING
                """;

        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);

            try {
                long tagId;

                try (PreparedStatement ps = connection.prepareStatement(tagSql)) {
                    ps.setString(1, tagName.trim().toLowerCase());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        tagId = rs.getLong("id");
                    }
                }

                try (PreparedStatement ps = connection.prepareStatement(linkSql)) {
                    ps.setLong(1, ticketId);
                    ps.setLong(2, tagId);
                    ps.executeUpdate();
                }

                connection.commit();

            } catch (Exception e) {
                connection.rollback();
                if (e instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new RuntimeException("Не удалось добавить тег", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Не удалось добавить тег", e);
        }
    }

    @Override
    public List<Ticket> findByTag(String tagName) {
        String sql = """
                SELECT t.*
                FROM tickets t
                JOIN ticket_tags tt ON tt.ticket_id = t.id
                JOIN tags tag ON tag.id = tt.tag_id
                WHERE tag.name = ?
                ORDER BY t.created_at DESC
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, tagName.trim().toLowerCase());

            try (ResultSet rs = ps.executeQuery()) {
                List<Ticket> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapTicket(rs));
                }
                return result;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска заявок по тегу", e);
        }
    }

    @Override
    public List<String> findTagsByTicketId(long ticketId) {
        String sql = """
                SELECT tag.name
                FROM tags tag
                JOIN ticket_tags tt ON tt.tag_id = tag.id
                WHERE tt.ticket_id = ?
                ORDER BY tag.name
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, ticketId);

            try (ResultSet rs = ps.executeQuery()) {
                List<String> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(rs.getString("name"));
                }
                return result;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения тегов заявки", e);
        }
    }

    private Ticket mapTicket(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setId(rs.getLong("id"));
        ticket.setTitle(rs.getString("title"));
        ticket.setDescription(rs.getString("description"));
        ticket.setStatus(TicketStatus.valueOf(rs.getString("status")));
        ticket.setPriority(TicketPriority.valueOf(rs.getString("priority")));
        ticket.setCustomerId(rs.getLong("customer_id"));

        long assigneeId = rs.getLong("assignee_id");
        ticket.setAssigneeId(rs.wasNull() ? null : assigneeId);

        long categoryId = rs.getLong("category_id");
        ticket.setCategoryId(rs.wasNull() ? null : categoryId);

        ticket.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        ticket.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        Timestamp closed = rs.getTimestamp("closed_at");
        ticket.setClosedAt(closed == null ? null : closed.toLocalDateTime());

        Timestamp due = rs.getTimestamp("due_at");
        ticket.setDueAt(due == null ? null : due.toLocalDateTime());

        ticket.setVersion(rs.getLong("version"));
        return ticket;
    }
}
