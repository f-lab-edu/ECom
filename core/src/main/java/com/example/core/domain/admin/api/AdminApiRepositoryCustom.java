package com.example.core.domain.admin.api;

import java.util.List;

import com.example.core.domain.role.Role;

public interface AdminApiRepositoryCustom {

    List<Role> findRolesByAdminId(Long adminId);
}
