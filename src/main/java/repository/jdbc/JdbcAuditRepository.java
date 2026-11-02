package repository.jdbc;

import config.DatabaseConfig;
import repository.AuditRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class JdbcAuditRepository implements AuditRepository {

    @Override
    public void log(Long userId, String action, String entityType, Long entityId, String details) {
        String sql = """
                INSERT INTO audit_log(user_id, action, entity_type, entity_id, details)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            if (userId == null) {
                ps.setNull(1, Types.BIGINT);
            } else {
                ps.setLong(1, userId);
            }
            ps.setString(2, action);
            ps.setString(3, entityType);
            if (entityId == null) {
                ps.setNull(4, Types.BIGINT);
            } else {
                ps.setLong(4, entityId);
            }
            ps.setString(5, details);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Не удалось записать audit log", e);
        }
    }

    @Override
    public List<String> findRecent(int limit) {
        String sql = """
                SELECT created_at, action, entity_type, entity_id, details
                FROM audit_log
                ORDER BY id DESC
                LIMIT ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                List<String> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(String.format("%s | %s | %s#%d | %s",
                            rs.getTimestamp("created_at"),
                            rs.getString("action"),
                            rs.getString("entity_type"),
                            rs.getLong("entity_id"),
                            rs.getString("details")));
                }
                return result;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения audit log", e);
        }
    }
}
