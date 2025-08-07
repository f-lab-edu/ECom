package com.example.core.domain.user_to_role.api;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.core.domain.user_to_role.UserRole;

public interface UserRoleApiRepository extends JpaRepository<UserRole, Long> {
}