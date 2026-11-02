package repository.jdbc;

import config.DatabaseConfig;
import model.TicketStatus;
import model.TicketStatusHistory;
import repository.StatusHistoryRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcStatusHistoryRepository implements StatusHistoryRepository {

    @Override
    public List<TicketStatusHistory> findByTicketId(long ticketId) {
        String sql = """
                SELECT h.id,
                       h.ticket_id,
                       h.old_status,
                       h.new_status,
                       h.changed_by_id,
                       h.changed_at,
                       u.name AS changed_by_name
                FROM ticket_status_history h
                LEFT JOIN users u ON u.id = h.changed_by_id
                WHERE h.ticket_id = ?
                ORDER BY h.changed_at, h.id
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, ticketId);

            try (ResultSet rs = ps.executeQuery()) {
                List<TicketStatusHistory> result = new ArrayList<>();
                while (rs.next()) {
                    TicketStatusHistory history = new TicketStatusHistory();
                    history.setId(rs.getLong("id"));
                    history.setTicketId(rs.getLong("ticket_id"));

                    String oldStatus = rs.getString("old_status");
                    history.setOldStatus(oldStatus == null ? null : TicketStatus.valueOf(oldStatus));
                    history.setNewStatus(TicketStatus.valueOf(rs.getString("new_status")));

                    long changedBy = rs.getLong("changed_by_id");
                    history.setChangedById(rs.wasNull() ? null : changedBy);
                    history.setChangedByName(rs.getString("changed_by_name"));
                    history.setChangedAt(rs.getTimestamp("changed_at").toLocalDateTime());
                    result.add(history);
                }
                return result;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения истории статусов", e);
        }
    }
}
