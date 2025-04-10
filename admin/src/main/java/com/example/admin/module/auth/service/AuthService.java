package com.example.admin.module.auth.service;

import com.example.admin.module.auth.controller.request.LoginReqBody;
import com.example.admin.module.auth.controller.request.SignupRequestBody;
import com.example.core.domain.admin.Admin;
import com.example.core.domain.admin.api.AdminApiRepository;
import com.example.core.domain.admin.meta.Status;
import com.example.core.domain.role.Role;
import com.example.core.exception.BadRequestException;
import com.example.core.model.AuthResponse;
import com.example.core.utils.JwtUtil;
import com.example.core.utils.MessageUtil;
import com.example.core.utils.SaltedHashUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminApiRepository adminApiRepository;

    private final JwtUtil jwtUtil;
    private final MessageUtil messageUtil;
    private final SaltedHashUtil saltedHashUtil;

    @Transactional
    public AuthResponse createAdmin(SignupRequestBody body) {
        if (adminApiRepository.existsAdminByEmail(body.getEmail())) {
            throw new BadRequestException(messageUtil.getMessage("auth.user.email.DUPLICATE"));
        }

        String salt = saltedHashUtil.generateSalt();
        String hashedPassword = saltedHashUtil.hashPassword(body.getPassword(), salt);

        Admin admin = Admin.of(body.getEmail(), body.getNickname(), salt, hashedPassword, body.getPhoneNumber());
        adminApiRepository.save(admin);

        return AuthResponse.from(admin);
    }


    @Transactional(readOnly = true)
    public AuthResponse login(HttpServletResponse response, LoginReqBody body) {
        Admin admin = adminApiRepository.findByEmail(body.getEmail())
                .orElseThrow(() -> new BadRequestException(messageUtil.getMessage("auth.admin.email.NOTFOUND")));

        Status adminStatus = admin.getStatus();
        if (adminStatus != Status.ACTIVE) {
            throw new BadRequestException(messageUtil.getMessage("auth.admin.INACTIVE", adminStatus));
        }

        if (!saltedHashUtil.verifyPassword(body.getPassword(), admin.getSalt(), admin.getHashedPassword())) {
            throw new BadRequestException(messageUtil.getMessage("auth.admin.password.INCORRECT"));
        }

        Long adminId = admin.getId();
        List<String> roles = getAdminRoles(adminId);
        setNewCookies(response, adminId, roles);

        return AuthResponse.from(admin);
    }

    @Transactional
    public AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtUtil.extractRefreshTokenFromCookie(request);

        if (!StringUtils.hasText(refreshToken)) {
            throw new BadCredentialsException(messageUtil.getMessage("jwt.EMPTY_TOKEN"));
        }

        // 토큰이 잘못된 경우
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new BadCredentialsException(messageUtil.getMessage("jwt.INVALID_TOKEN"));
        }

        Claims claims = jwtUtil.extractRefreshClaims(refreshToken);

        // 토큰 타입이 refresh가 아닌 경우
        if (!claims.get("type", String.class).equals("refresh")) {
            throw new BadCredentialsException(messageUtil.getMessage("jwt.WRONG_TYPE_TOKEN"));
        }

        Long adminId = claims.get("userId", Long.class);

        Admin admin = adminApiRepository.findById(adminId)
                .orElseThrow(() -> new BadRequestException(messageUtil.getMessage("auth.user.email.NOTFOUND")));
        Status adminStatus = admin.getStatus();
        if (adminStatus != Status.ACTIVE) {
            throw new BadRequestException(messageUtil.getMessage("auth.user.INACTIVE", adminStatus));
        }

        List<String> roles = getAdminRoles(adminId);
        setNewCookies(response, adminId, roles);

        return AuthResponse.from(admin);
    }

    private void setNewCookies(HttpServletResponse response, Long adminId, List<String> roles) {
        String accessToken = jwtUtil.createAccessToken(adminId, roles);
        String refreshToken = jwtUtil.createRefreshToken(adminId, roles);
        jwtUtil.setTokenCookies(response, accessToken, refreshToken);
    }

    @Transactional(readOnly = true)
    public List<String> getAdminRoles(Long adminId) {
        List<Role> roles = adminApiRepository.findRolesByAdminId(adminId);
        return roles.stream()
                .map(Role::getDescription)
                .toList();
    }
}
