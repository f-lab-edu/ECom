package com.example.core.domain.admin_to_role.api;

import com.example.core.domain.admin_to_role.AdminRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRoleApiRepository extends JpaRepository<AdminRole, Long> {
}
