package com.studentrisk.backend.repository;

import com.studentrisk.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByStudentId(Long studentId);

    boolean existsByUsernameIgnoreCase(String username);
}
