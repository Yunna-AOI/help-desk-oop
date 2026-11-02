package repository.jdbc;

import config.DatabaseConfig;
import model.SlaPolicy;
import model.TicketPriority;
import repository.SlaPolicyRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcSlaPolicyRepository implements SlaPolicyRepository {

    @Override
    public Optional<SlaPolicy> findByPriority(TicketPriority priority) {
        String sql = "SELECT * FROM sla_policies WHERE priority = ?::ticket_priority";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, priority.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapPolicy(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения SLA-политики", e);
        }
    }

    @Override
    public List<SlaPolicy> findAll() {
        String sql = "SELECT * FROM sla_policies ORDER BY id";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<SlaPolicy> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapPolicy(rs));
            }
            return result;

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения SLA-политик", e);
        }
    }

    private SlaPolicy mapPolicy(ResultSet rs) throws SQLException {
        SlaPolicy policy = new SlaPolicy();
        policy.setId(rs.getLong("id"));
        policy.setName(rs.getString("name"));
        policy.setPriority(TicketPriority.valueOf(rs.getString("priority")));
        policy.setResponseMinutes(rs.getInt("response_minutes"));
        policy.setResolveMinutes(rs.getInt("resolve_minutes"));
        return policy;
    }
}
