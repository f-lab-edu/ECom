package com.example.core.domain.admin.api;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.core.domain.admin_to_role.QAdminRole;
import com.example.core.domain.role.QRole;
import com.example.core.domain.role.Role;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class AdminApiRepositoryImpl implements AdminApiRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Role> findRolesByAdminId(Long adminId) {

        QAdminRole adminRole = QAdminRole.adminRole;
        QRole role = QRole.role;

        return queryFactory
                .selectDistinct(role)
                .from(adminRole)
                .join(adminRole.role, role)
                .where(adminRole.admin.id.eq(adminId))
                .fetch();
    }
}
