package com.project2.repository;

import com.project2.model.User;
import com.project2.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByRoleAndIsActive(Role role, Boolean isActive);

    List<User> findByRoleAndIsActiveAndCategoryContainingIgnoreCase(Role role, Boolean isActive, String category);

    Optional<User> findByResetToken(String resetToken);
}
