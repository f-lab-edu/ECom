package com.example.core.domain.permission;

import com.example.core.domain.BaseEntity;
import com.example.core.domain.role_to_permission.RolePermission;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Entity
@Table
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Permission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @OneToMany
    @JoinColumn(name = "permission_id")
    private List<RolePermission> rolePermissions;

}
