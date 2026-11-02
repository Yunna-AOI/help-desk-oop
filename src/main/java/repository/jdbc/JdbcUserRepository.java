package repository.jdbc;

import config.DatabaseConfig;
import model.Administrator;
import model.Customer;
import model.SupportAgent;
import model.User;
import model.UserRole;
import repository.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {

    @Override
    public User save(User user) {
        String sql = """
                INSERT INTO users(name, email, role, department_id, active)
                VALUES (?, ?, ?::user_role, ?, ?)
                RETURNING id, created_at
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole().name());

            if (user.getDepartmentId() == null) {
                ps.setNull(4, Types.BIGINT);
            } else {
                ps.setLong(4, user.getDepartmentId());
            }

            ps.setBoolean(5, user.isActive());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user.setId(rs.getLong("id"));
                    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
            }
            return user;

        } catch (SQLException e) {
            throw new RuntimeException("Не удалось сохранить пользователя", e);
        }
    }

    @Override
    public Optional<User> findById(long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return findOne(sql, id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        return findOne(sql, email);
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY id";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<User> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapUser(rs));
            }
            return result;

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения пользователей", e);
        }
    }

    @Override
    public List<User> findByRole(UserRole role) {
        String sql = "SELECT * FROM users WHERE role = ?::user_role ORDER BY id";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, role.name());

            try (ResultSet rs = ps.executeQuery()) {
                List<User> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapUser(rs));
                }
                return result;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения пользователей по роли", e);
        }
    }

    private Optional<User> findOne(String sql, Object parameter) {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            if (parameter instanceof Long value) {
                ps.setLong(1, value);
            } else {
                ps.setString(1, (String) parameter);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapUser(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска пользователя", e);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        UserRole role = UserRole.valueOf(rs.getString("role"));

        User user = switch (role) {
            case CUSTOMER -> new Customer();
            case SUPPORT_AGENT -> new SupportAgent();
            case ADMIN -> new Administrator();
        };

        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setRole(role);

        long departmentId = rs.getLong("department_id");
        user.setDepartmentId(rs.wasNull() ? null : departmentId);

        user.setActive(rs.getBoolean("active"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return user;
    }
}
