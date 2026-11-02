package repository;

import model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Category save(Category category);

    Optional<Category> findById(long id);

    List<Category> findAll();

    boolean deactivate(long id);
}
