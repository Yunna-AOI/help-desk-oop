package repository.jdbc;

import config.DatabaseConfig;
import model.TicketComment;
import model.UserRole;
import repository.CommentRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcCommentRepository implements CommentRepository {

    @Override
    public TicketComment add(TicketComment comment) {
        String sql = """
                INSERT INTO ticket_comments(ticket_id, author_id, text, internal)
                VALUES (?, ?, ?, ?)
                RETURNING id, created_at
                """;
        String auditSql = """
                INSERT INTO audit_log(user_id, action, entity_type, entity_id, details)
                VALUES (?, 'ADD_COMMENT', 'TICKET', ?, ?)
                """;

        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);

            try {
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setLong(1, comment.getTicketId());
                    ps.setLong(2, comment.getAuthorId());
                    ps.setString(3, comment.getText());
                    ps.setBoolean(4, comment.isInternal());

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            comment.setId(rs.getLong("id"));
                            comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                        }
                    }
                }

                try (PreparedStatement ps = connection.prepareStatement(auditSql)) {
                    ps.setLong(1, comment.getAuthorId());
                    ps.setLong(2, comment.getTicketId());
                    ps.setString(3, comment.isInternal() ? "internal" : "external");
                    ps.executeUpdate();
                }

                connection.commit();
                return comment;

            } catch (Exception e) {
                connection.rollback();
                if (e instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new RuntimeException("Не удалось добавить комментарий", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Не удалось добавить комментарий", e);
        }
    }

    @Override
    public List<TicketComment> findByTicketId(long ticketId) {
        String sql = """
                SELECT c.id,
                       c.ticket_id,
                       c.author_id,
                       c.text,
                       c.internal,
                       c.created_at,
                       u.name AS author_name,
                       u.role AS author_role
                FROM ticket_comments c
                JOIN users u ON u.id = c.author_id
                WHERE c.ticket_id = ?
                ORDER BY c.created_at
                """;

        return selectComments(sql, ticketId);
    }

    @Override
    public List<TicketComment> findExternalByTicketId(long ticketId) {
        String sql = """
                SELECT c.id,
                       c.ticket_id,
                       c.author_id,
                       c.text,
                       c.internal,
                       c.created_at,
                       u.name AS author_name,
                       u.role AS author_role
                FROM ticket_comments c
                JOIN users u ON u.id = c.author_id
                WHERE c.ticket_id = ? AND c.internal = FALSE
                ORDER BY c.created_at
                """;

        return selectComments(sql, ticketId);
    }

    private List<TicketComment> selectComments(String sql, long ticketId) {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, ticketId);

            try (ResultSet rs = ps.executeQuery()) {
                List<TicketComment> result = new ArrayList<>();
                while (rs.next()) {
                    TicketComment comment = new TicketComment();
                    comment.setId(rs.getLong("id"));
                    comment.setTicketId(rs.getLong("ticket_id"));
                    comment.setAuthorId(rs.getLong("author_id"));
                    comment.setText(rs.getString("text"));
                    comment.setInternal(rs.getBoolean("internal"));
                    comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    comment.setAuthorName(rs.getString("author_name"));
                    comment.setAuthorRole(UserRole.valueOf(rs.getString("author_role")));
                    result.add(comment);
                }
                return result;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения комментариев", e);
        }
    }
}
