package com.example.core.domain.admin.api;

import com.example.core.domain.role.Role;

import java.util.List;

public interface AdminApiRepositoryCustom {

    List<Role> findRolesByAdminId(Long adminId);
}
