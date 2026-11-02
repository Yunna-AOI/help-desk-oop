package repository;

import model.User;
import model.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(long id);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    List<User> findByRole(UserRole role);
}
