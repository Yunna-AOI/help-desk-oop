package repository.jdbc;

import config.DatabaseConfig;
import model.Category;
import model.TicketPriority;
import repository.CategoryRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCategoryRepository implements CategoryRepository {

    @Override
    public Category save(Category category) {
        String sql = """
                INSERT INTO categories(name, description, default_priority, active)
                VALUES (?, ?, ?::ticket_priority, ?)
                RETURNING id
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setString(3, category.getDefaultPriority().name());
            ps.setBoolean(4, category.isActive());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    category.setId(rs.getLong("id"));
                }
            }
            return category;

        } catch (SQLException e) {
            throw new RuntimeException("Не удалось сохранить категорию", e);
        }
    }

    @Override
    public Optional<Category> findById(long id) {
        String sql = "SELECT * FROM categories WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapCategory(rs)) : Optional.empty();
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска категории", e);
        }
    }

    @Override
    public List<Category> findAll() {
        String sql = "SELECT * FROM categories ORDER BY id";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Category> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapCategory(rs));
            }
            return result;

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения категорий", e);
        }
    }

    @Override
    public boolean deactivate(long id) {
        String sql = "UPDATE categories SET active = FALSE WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Не удалось деактивировать категорию", e);
        }
    }

    private Category mapCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setId(rs.getLong("id"));
        category.setName(rs.getString("name"));
        category.setDescription(rs.getString("description"));
        category.setDefaultPriority(TicketPriority.valueOf(rs.getString("default_priority")));
        category.setActive(rs.getBoolean("active"));
        return category;
    }
}
