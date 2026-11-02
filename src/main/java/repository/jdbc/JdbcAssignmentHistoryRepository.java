package repository.jdbc;

import config.DatabaseConfig;
import model.TicketAssignmentHistory;
import repository.AssignmentHistoryRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcAssignmentHistoryRepository implements AssignmentHistoryRepository {

    @Override
    public List<TicketAssignmentHistory> findByTicketId(long ticketId) {
        String sql = """
                SELECT h.id,
                       h.ticket_id,
                       h.old_assignee_id,
                       old_user.name AS old_assignee_name,
                       h.new_assignee_id,
                       new_user.name AS new_assignee_name,
                       h.changed_by_id,
                       changed_user.name AS changed_by_name,
                       h.changed_at
                FROM ticket_assignment_history h
                LEFT JOIN users old_user ON old_user.id = h.old_assignee_id
                LEFT JOIN users new_user ON new_user.id = h.new_assignee_id
                LEFT JOIN users changed_user ON changed_user.id = h.changed_by_id
                WHERE h.ticket_id = ?
                ORDER BY h.changed_at, h.id
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, ticketId);

            try (ResultSet rs = ps.executeQuery()) {
                List<TicketAssignmentHistory> result = new ArrayList<>();
                while (rs.next()) {
                    TicketAssignmentHistory history = new TicketAssignmentHistory();
                    history.setId(rs.getLong("id"));
                    history.setTicketId(rs.getLong("ticket_id"));

                    long oldAssigneeId = rs.getLong("old_assignee_id");
                    history.setOldAssigneeId(rs.wasNull() ? null : oldAssigneeId);
                    history.setOldAssigneeName(rs.getString("old_assignee_name"));

                    long newAssigneeId = rs.getLong("new_assignee_id");
                    history.setNewAssigneeId(rs.wasNull() ? null : newAssigneeId);
                    history.setNewAssigneeName(rs.getString("new_assignee_name"));

                    long changedById = rs.getLong("changed_by_id");
                    history.setChangedById(rs.wasNull() ? null : changedById);
                    history.setChangedByName(rs.getString("changed_by_name"));
                    history.setChangedAt(rs.getTimestamp("changed_at").toLocalDateTime());
                    result.add(history);
                }
                return result;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения истории назначений", e);
        }
    }
}
