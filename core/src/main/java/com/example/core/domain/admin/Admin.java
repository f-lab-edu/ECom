package com.example.core.domain.admin;

import com.example.core.domain.BaseEntity;
import com.example.core.domain.admin.meta.Status;
import com.example.core.domain.admin_to_role.AdminRole;
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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column
    private String nickname;

    @Column(nullable = false)
    private String salt;

    @Column(nullable = false)
    private String hashedPassword;

    @Column(nullable = false)
    private String phoneNumber;

    @Column
    private Status status;

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AdminRole> adminRoles;


    public static Admin of(String email,
                           String nickname,
                           String salt,
                           String hashedPassword,
                           String phoneNumber) {
        return Admin.builder()
                   .email(email)
                   .nickname(nickname)
                   .salt(salt)
                   .hashedPassword(hashedPassword)
                   .phoneNumber(phoneNumber)
                   .status(Status.ACTIVE)
                   .build();
    }
}
