package com.example.core.domain.role.api;

import com.example.core.domain.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleApiRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByDescription(String description);
}
