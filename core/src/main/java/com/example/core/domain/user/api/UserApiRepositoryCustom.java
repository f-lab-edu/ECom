package com.example.core.domain.user.api;

import java.util.List;

import com.example.core.domain.role.Role;

public interface UserApiRepositoryCustom {

    List<Role> findRolesByUserId(Long userId);
}