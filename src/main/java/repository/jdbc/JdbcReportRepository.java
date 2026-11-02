package repository.jdbc;

import config.DatabaseConfig;
import model.AgentStats;
import repository.ReportRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JdbcReportRepository implements ReportRepository {

    @Override
    public Map<String, Long> countByStatus() {
        String sql = """
                SELECT status::text AS status, COUNT(*) AS count
                FROM tickets
                GROUP BY status
                ORDER BY status
                """;

        Map<String, Long> result = new LinkedHashMap<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.put(rs.getString("status"), rs.getLong("count"));
            }
            return result;

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка отчёта по статусам", e);
        }
    }

    @Override
    public Map<String, Long> countByCategory() {
        String sql = """
                SELECT COALESCE(c.name, 'Без категории') AS name,
                       COUNT(t.id) AS ticket_count
                FROM tickets t
                LEFT JOIN categories c ON c.id = t.category_id
                GROUP BY c.name
                ORDER BY ticket_count DESC
                """;

        Map<String, Long> result = new LinkedHashMap<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.put(rs.getString("name"), rs.getLong("ticket_count"));
            }
            return result;

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка отчёта по категориям", e);
        }
    }

    @Override
    public List<AgentStats> agentStats() {
        String sql = """
                SELECT u.name,
                       COUNT(t.id) FILTER (
                           WHERE t.status IN ('NEW', 'IN_PROGRESS')
                       ) AS active_tickets,
                       COUNT(t.id) FILTER (
                           WHERE t.status = 'RESOLVED'
                       ) AS resolved_tickets,
                       COUNT(t.id) FILTER (
                           WHERE t.status = 'CLOSED'
                       ) AS closed_tickets
                FROM users u
                LEFT JOIN tickets t ON t.assignee_id = u.id
                WHERE u.role = 'SUPPORT_AGENT'
                GROUP BY u.id, u.name
                ORDER BY active_tickets DESC, u.name
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<AgentStats> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new AgentStats(
                        rs.getString("name"),
                        rs.getLong("active_tickets"),
                        rs.getLong("resolved_tickets"),
                        rs.getLong("closed_tickets")
                ));
            }
            return result;

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка отчёта по сотрудникам", e);
        }
    }

    @Override
    public Map<String, String> averageResolutionByCategory() {
        String sql = """
                SELECT COALESCE(c.name, 'Без категории') AS name,
                       AVG(EXTRACT(EPOCH FROM (t.closed_at - t.created_at))) AS seconds
                FROM tickets t
                LEFT JOIN categories c ON c.id = t.category_id
                WHERE t.closed_at IS NOT NULL
                GROUP BY c.name
                ORDER BY c.name
                """;

        Map<String, String> result = new LinkedHashMap<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                double seconds = rs.getDouble("seconds");
                result.put(rs.getString("name"), formatDuration((long) seconds));
            }
            return result;

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка отчёта по времени решения", e);
        }
    }

    private String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%d ч %02d мин %02d сек", hours, minutes, seconds);
    }
}
