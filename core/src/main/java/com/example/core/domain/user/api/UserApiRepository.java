package com.example.core.domain.user.api;

import com.example.core.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserApiRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsUserByEmail(String email);
}
