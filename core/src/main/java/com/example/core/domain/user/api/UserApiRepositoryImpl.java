package com.example.core.domain.user.api;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.core.domain.role.QRole;
import com.example.core.domain.role.Role;
import com.example.core.domain.user_to_role.QUserRole;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class UserApiRepositoryImpl implements UserApiRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Role> findRolesByUserId(Long userId) {

        QUserRole userRole = QUserRole.userRole;
        QRole role = QRole.role;

        return queryFactory
                .selectDistinct(role)
                .from(userRole)
                .join(userRole.role, role)
                .where(userRole.user.id.eq(userId))
                .fetch();
    }
}