package repository.jdbc;

import config.DatabaseConfig;
import model.Ticket;
import model.TicketDetails;
import model.TicketPriority;
import model.TicketStatus;
import repository.TicketRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTicketRepository implements TicketRepository {

    @Override
    public Ticket save(Ticket ticket) {
        String sql = """
                INSERT INTO tickets(
                    title, description, status, priority,
                    customer_id, assignee_id, category_id, due_at
                )
                VALUES (?, ?, ?::ticket_status, ?::ticket_priority, ?, ?, ?, ?)
                RETURNING id, created_at, updated_at, version
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, ticket.getTitle());
            ps.setString(2, ticket.getDescription());
            ps.setString(3, ticket.getStatus().name());
            ps.setString(4, ticket.getPriority().name());
            ps.setLong(5, ticket.getCustomerId());
            setNullableLong(ps, 6, ticket.getAssigneeId());
            setNullableLong(ps, 7, ticket.getCategoryId());
            setNullableTimestamp(ps, 8, ticket.getDueAt());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ticket.setId(rs.getLong("id"));
                    ticket.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    ticket.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    ticket.setVersion(rs.getLong("version"));
                }
            }
            return ticket;

        } catch (SQLException e) {
            throw new RuntimeException("Не удалось сохранить заявку", e);
        }
    }

    @Override
    public Optional<Ticket> findById(long id) {
        String sql = "SELECT * FROM tickets WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapTicket(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска заявки", e);
        }
    }

    @Override
    public List<Ticket> findAll() {
        return selectTickets("SELECT * FROM tickets ORDER BY id", null);
    }

    @Override
    public List<Ticket> findByStatus(TicketStatus status) {
        String sql = """
                SELECT * FROM tickets
                WHERE status = ?::ticket_status
                ORDER BY created_at DESC
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, status.name());
            return readTickets(ps);

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка фильтрации заявок по статусу", e);
        }
    }

    @Override
    public List<Ticket> findByStatusAndPriority(TicketStatus status, TicketPriority priority) {
        StringBuilder sql = new StringBuilder("SELECT * FROM tickets WHERE 1 = 1");

        if (status != null) {
            sql.append(" AND status = ?::ticket_status");
        }
        if (priority != null) {
            sql.append(" AND priority = ?::ticket_priority");
        }
        sql.append(" ORDER BY created_at DESC");

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {

            int index = 1;
            if (status != null) {
                ps.setString(index++, status.name());
            }
            if (priority != null) {
                ps.setString(index, priority.name());
            }
            return readTickets(ps);

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка фильтрации заявок", e);
        }
    }

    @Override
    public List<Ticket> findByCustomerId(long customerId) {
        String sql = """
                SELECT * FROM tickets
                WHERE customer_id = ?
                ORDER BY created_at DESC
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, customerId);
            return readTickets(ps);

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска заявок клиента", e);
        }
    }

    @Override
    public List<Ticket> findPage(int page, int size) {
        if (page < 1 || size < 1) {
            throw new IllegalArgumentException("page и size должны быть больше нуля");
        }

        String sql = """
                SELECT * FROM tickets
                ORDER BY id
                LIMIT ? OFFSET ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, size);
            ps.setInt(2, (page - 1) * size);
            return readTickets(ps);

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка постраничного чтения заявок", e);
        }
    }

    @Override
    public List<Ticket> search(String text) {
        String sql = """
                SELECT * FROM tickets
                WHERE title ILIKE ? OR description ILIKE ?
                ORDER BY created_at DESC
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            String pattern = "%" + text + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            return readTickets(ps);

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска заявок", e);
        }
    }

    @Override
    public List<Ticket> findOverdue() {
        String sql = """
                SELECT * FROM tickets
                WHERE due_at IS NOT NULL
                  AND due_at < CURRENT_TIMESTAMP
                  AND status NOT IN ('CLOSED', 'CANCELLED')
                ORDER BY due_at
                """;

        return selectTickets(sql, null);
    }

    @Override
    public List<TicketDetails> findDetails() {
        String sql = "SELECT * FROM ticket_details ORDER BY id";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<TicketDetails> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapDetails(rs));
            }
            return result;

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения представления ticket_details", e);
        }
    }

    @Override
    public Optional<TicketDetails> findDetailsById(long id) {
        String sql = "SELECT * FROM ticket_details WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapDetails(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения карточки заявки", e);
        }
    }

    @Override
    public void update(Ticket ticket) {
        String sql = """
                UPDATE tickets
                SET title = ?,
                    description = ?,
                    status = ?::ticket_status,
                    priority = ?::ticket_priority,
                    assignee_id = ?,
                    category_id = ?,
                    due_at = ?,
                    closed_at = ?,
                    updated_at = CURRENT_TIMESTAMP,
                    version = version + 1
                WHERE id = ? AND version = ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, ticket.getTitle());
            ps.setString(2, ticket.getDescription());
            ps.setString(3, ticket.getStatus().name());
            ps.setString(4, ticket.getPriority().name());
            setNullableLong(ps, 5, ticket.getAssigneeId());
            setNullableLong(ps, 6, ticket.getCategoryId());
            setNullableTimestamp(ps, 7, ticket.getDueAt());
            setNullableTimestamp(ps, 8, ticket.getClosedAt());
            ps.setLong(9, ticket.getId());
            ps.setLong(10, ticket.getVersion());

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new IllegalStateException(
                        "Заявка уже изменена другим пользователем или не существует"
                );
            }
            ticket.setVersion(ticket.getVersion() + 1);

        } catch (SQLException e) {
            throw new RuntimeException("Не удалось обновить заявку", e);
        }
    }

    @Override
    public boolean deleteById(long id) {
        String sql = "DELETE FROM tickets WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Не удалось удалить заявку", e);
        }
    }

    @Override
    public boolean changeStatus(long ticketId, TicketStatus newStatus, long changedByUserId) {
        String selectSql = "SELECT status FROM tickets WHERE id = ? FOR UPDATE";
        String updateSql = """
                UPDATE tickets
                SET status = ?::ticket_status,
                    updated_at = CURRENT_TIMESTAMP,
                    closed_at = CASE
                        WHEN ?::ticket_status = 'CLOSED' THEN CURRENT_TIMESTAMP
                        ELSE closed_at
                    END,
                    version = version + 1
                WHERE id = ?
                """;
        String historySql = """
                INSERT INTO ticket_status_history(
                    ticket_id, old_status, new_status, changed_by_id
                )
                VALUES (?, ?::ticket_status, ?::ticket_status, ?)
                """;

        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);

            try {
                TicketStatus oldStatus;

                try (PreparedStatement ps = connection.prepareStatement(selectSql)) {
                    ps.setLong(1, ticketId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Заявка не найдена: " + ticketId);
                        }
                        oldStatus = TicketStatus.valueOf(rs.getString("status"));
                    }
                }

                if (!oldStatus.canTransitionTo(newStatus)) {
                    throw new IllegalStateException(
                            "Недопустимый переход: " + oldStatus + " -> " + newStatus
                    );
                }

                try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
                    ps.setString(1, newStatus.name());
                    ps.setString(2, newStatus.name());
                    ps.setLong(3, ticketId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = connection.prepareStatement(historySql)) {
                    ps.setLong(1, ticketId);
                    ps.setString(2, oldStatus.name());
                    ps.setString(3, newStatus.name());
                    ps.setLong(4, changedByUserId);
                    ps.executeUpdate();
                }

                insertAudit(connection, changedByUserId, "CHANGE_STATUS",
                        "TICKET", ticketId, oldStatus + " -> " + newStatus);

                connection.commit();
                return true;

            } catch (Exception e) {
                connection.rollback();
                if (e instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new RuntimeException("Ошибка изменения статуса", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка изменения статуса", e);
        }
    }

    @Override
    public boolean assign(long ticketId, long agentId, long changedByUserId) {
        String agentSql = "SELECT role, active FROM users WHERE id = ?";
        String ticketSql = "SELECT assignee_id, status FROM tickets WHERE id = ? FOR UPDATE";
        String updateSql = """
                UPDATE tickets
                SET assignee_id = ?,
                    status = CASE
                        WHEN status = 'NEW' THEN 'IN_PROGRESS'::ticket_status
                        ELSE status
                    END,
                    updated_at = CURRENT_TIMESTAMP,
                    version = version + 1
                WHERE id = ?
                """;
        String assignmentHistorySql = """
                INSERT INTO ticket_assignment_history(
                    ticket_id, old_assignee_id, new_assignee_id, changed_by_id
                )
                VALUES (?, ?, ?, ?)
                """;
        String statusHistorySql = """
                INSERT INTO ticket_status_history(
                    ticket_id, old_status, new_status, changed_by_id
                )
                VALUES (?, 'NEW'::ticket_status, 'IN_PROGRESS'::ticket_status, ?)
                """;

        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);

            try {
                String role;
                boolean active;

                try (PreparedStatement ps = connection.prepareStatement(agentSql)) {
                    ps.setLong(1, agentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Пользователь не найден: " + agentId);
                        }
                        role = rs.getString("role");
                        active = rs.getBoolean("active");
                    }
                }

                if (!"SUPPORT_AGENT".equals(role) || !active) {
                    throw new IllegalStateException(
                            "Назначать заявку можно только активному SUPPORT_AGENT"
                    );
                }

                Long oldAssigneeId;
                TicketStatus oldStatus;

                try (PreparedStatement ps = connection.prepareStatement(ticketSql)) {
                    ps.setLong(1, ticketId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException("Заявка не найдена: " + ticketId);
                        }
                        long oldAssignee = rs.getLong("assignee_id");
                        oldAssigneeId = rs.wasNull() ? null : oldAssignee;
                        oldStatus = TicketStatus.valueOf(rs.getString("status"));
                    }
                }

                try (PreparedStatement ps = connection.prepareStatement(updateSql)) {
                    ps.setLong(1, agentId);
                    ps.setLong(2, ticketId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = connection.prepareStatement(assignmentHistorySql)) {
                    ps.setLong(1, ticketId);
                    setNullableLong(ps, 2, oldAssigneeId);
                    ps.setLong(3, agentId);
                    ps.setLong(4, changedByUserId);
                    ps.executeUpdate();
                }

                if (oldStatus == TicketStatus.NEW) {
                    try (PreparedStatement ps = connection.prepareStatement(statusHistorySql)) {
                        ps.setLong(1, ticketId);
                        ps.setLong(2, changedByUserId);
                        ps.executeUpdate();
                    }
                }

                insertAudit(connection, changedByUserId, "ASSIGN_TICKET",
                        "TICKET", ticketId, "assignee=" + agentId);

                connection.commit();
                return true;

            } catch (Exception e) {
                connection.rollback();
                if (e instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new RuntimeException("Ошибка назначения заявки", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка назначения заявки", e);
        }
    }

    private List<Ticket> selectTickets(String sql, Long parameter) {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            if (parameter != null) {
                ps.setLong(1, parameter);
            }
            return readTickets(ps);

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения заявок", e);
        }
    }

    private List<Ticket> readTickets(PreparedStatement ps) throws SQLException {
        List<Ticket> result = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapTicket(rs));
            }
        }
        return result;
    }

    private TicketDetails mapDetails(ResultSet rs) throws SQLException {
        TicketDetails details = new TicketDetails();
        details.setId(rs.getLong("id"));
        details.setTitle(rs.getString("title"));
        details.setStatus(TicketStatus.valueOf(rs.getString("status")));
        details.setPriority(TicketPriority.valueOf(rs.getString("priority")));
        details.setCategoryName(rs.getString("category_name"));
        details.setCustomerName(rs.getString("customer_name"));
        details.setAssigneeName(rs.getString("assignee_name"));
        details.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        details.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        Timestamp due = rs.getTimestamp("due_at");
        details.setDueAt(due == null ? null : due.toLocalDateTime());
        return details;
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

    private void insertAudit(Connection connection, long userId, String action,
                             String entityType, long entityId, String details)
            throws SQLException {
        String sql = """
                INSERT INTO audit_log(user_id, action, entity_type, entity_id, details)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, action);
            ps.setString(3, entityType);
            ps.setLong(4, entityId);
            ps.setString(5, details);
            ps.executeUpdate();
        }
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.BIGINT);
        } else {
            ps.setLong(index, value);
        }
    }

    private void setNullableTimestamp(PreparedStatement ps, int index,
                                      java.time.LocalDateTime value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(index, Timestamp.valueOf(value));
        }
    }
}
