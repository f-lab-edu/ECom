package com.example.core.domain.admin.api;

import com.example.core.domain.admin.Admin;
import com.example.core.domain.role.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AdminApiRepository extends JpaRepository<Admin, Long>, AdminApiRepositoryCustom{
    Optional<Admin> findByEmail(String email);


}
