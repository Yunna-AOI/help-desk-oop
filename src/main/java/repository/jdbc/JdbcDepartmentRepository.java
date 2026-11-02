package repository.jdbc;

import config.DatabaseConfig;
import model.Department;
import repository.DepartmentRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcDepartmentRepository implements DepartmentRepository {

    @Override
    public List<Department> findAll() {
        String sql = "SELECT * FROM departments ORDER BY id";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Department> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapDepartment(rs));
            }
            return result;

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения подразделений", e);
        }
    }

    @Override
    public Optional<Department> findById(long id) {
        String sql = "SELECT * FROM departments WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapDepartment(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска подразделения", e);
        }
    }

    private Department mapDepartment(ResultSet rs) throws SQLException {
        Department department = new Department();
        department.setId(rs.getLong("id"));
        department.setName(rs.getString("name"));
        department.setDescription(rs.getString("description"));
        department.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return department;
    }
}
