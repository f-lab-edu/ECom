package com.example.core.init;

import com.example.core.domain.admin.Admin;
import com.example.core.domain.admin.api.AdminApiRepository;
import com.example.core.domain.admin_to_role.AdminRole;
import com.example.core.domain.cart.Cart;
import com.example.core.domain.cart.api.CartApiRepository;
import com.example.core.domain.role.Role;
import com.example.core.domain.role.api.RoleApiRepository;
import com.example.core.domain.user.api.UserApiRepository;
import com.example.core.utils.SaltedHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile({"api-local","api-test","admin-local","admin-test"})
@RequiredArgsConstructor
public class InitDataLoader implements CommandLineRunner {

    private final RoleApiRepository roleApiRepository;
    private final AdminApiRepository adminApiRepository;
    private final UserApiRepository userApiRepository;
    private final CartApiRepository cartApiRepository;

    private final SaltedHashUtil saltedHashUtil;

    @Override
    public void run(String... args) {

        // 역할 데이터 초기화
        System.out.println("[InitDataLoader] 초기화 시작");
        if (roleApiRepository.count()==0) {
            System.out.println("[InitDataLoader] Role이 비어있음. 데이터 삽입 시작");
            roleApiRepository.saveAll(List.of(
                    new Role("ROLE_SUPER_ADMIN")
                    , new Role("ROLE_ADMIN")
                    , new Role("ROLE_USER")
            ));
        }

        // 관리자 데이터 초기화
        if (adminApiRepository.count()==0) {
            System.out.println("[InitDataLoader] Admin이 비어있음. 데이터 삽입 시작");

            String salt = saltedHashUtil.generateSalt();
            String hashedPassword = saltedHashUtil.hashPassword("Ab1!2345", salt);

            Admin admin = Admin.of(
                    "superAdmin_12345@example.com",
                    "super_admin",
                    salt,
                    hashedPassword,
                    "010-1234-5678"
            );
            admin = adminApiRepository.save(admin);

            Role superAdminRole = roleApiRepository.findByDescription("ROLE_SUPER_ADMIN")
                    .orElseThrow(() -> new IllegalStateException("ROLE_SUPER_ADMIN 이 존재하지 않습니다."));

            AdminRole adminRole = new AdminRole();
            adminRole.setAdmin(admin);
            adminRole.setRole(superAdminRole);

            admin.setAdminRoles(List.of(adminRole));
            adminApiRepository.save(admin);
        }

        // 사용자 데이터 초기화
        if (userApiRepository.count()==0) {
            System.out.println("[InitDataLoader] User가 비어있음. 데이터 삽입 시작");
            // 사용자 데이터 삽입 로직

            String salt = saltedHashUtil.generateSalt();
            String hashedPassword = saltedHashUtil.hashPassword("Ab1!2345", salt);

            var user = com.example.core.domain.user.User.of(
                    "testUser_12345@example.com",
                    "testUser",
                    salt,
                    hashedPassword,
                    "010-1234-5678"
            );


            userApiRepository.save(user);
            cartApiRepository.save(Cart.of(user.getId()));
        }
    }
}
