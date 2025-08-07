package com.example.core.domain.user.api;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.core.domain.user.User;

public interface UserApiRepository extends JpaRepository<User, Long>, UserApiRepositoryCustom {

    Optional<User> findByEmail(String email);

    boolean existsUserByEmail(String email);
}
