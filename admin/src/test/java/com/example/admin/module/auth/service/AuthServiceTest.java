package com.example.admin.module.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.example.admin.module.auth.controller.request.LoginReqBody;
import com.example.admin.module.auth.controller.request.SignupRequestBody;
import com.example.core.domain.admin.Admin;
import com.example.core.domain.admin.api.AdminApiRepository;
import com.example.core.domain.admin.meta.Status;
import com.example.core.domain.admin_to_role.AdminRole;
import com.example.core.domain.admin_to_role.api.AdminRoleApiRepository;
import com.example.core.domain.role.Role;
import com.example.core.domain.role.api.RoleApiRepository;
import com.example.core.exception.BadRequestException;
import com.example.core.model.AuthResponse;
import com.example.core.utils.JwtUtil;
import com.example.core.utils.MessageUtil;
import com.example.core.utils.SaltedHashUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;

@SpringBootTest
@ActiveProfiles({"admin-test", "core-test"})
@Transactional
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @MockitoBean
    private AdminApiRepository adminApiRepository;

    @MockitoBean
    private AdminRoleApiRepository adminRoleApiRepository;

    @MockitoBean
    private RoleApiRepository roleApiRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private MessageUtil messageUtil;

    @MockitoBean
    private SaltedHashUtil saltedHashUtil;

    private Admin testAdmin;
    private Role testRole;
    private AdminRole testAdminRole;
    private SignupRequestBody testSignupRequest;
    private LoginReqBody testLoginRequest;
    private Long testAdminId = 1L;

    @BeforeEach
    void setUp() {
        // 테스트 관리자 생성
        testAdmin = Admin.builder()
                .id(testAdminId)
                .email("admin@example.com")
                .nickname("Test Admin")
                .salt("adminSalt")
                .hashedPassword("hashedAdminPassword")
                .phoneNumber("010-1234-5678")
                .status(Status.ACTIVE)
                .build();

        // 테스트 역할 생성
        testRole = new Role();
        testRole.setId(1L);
        testRole.setDescription("ROLE_ADMIN");

        // 테스트 관리자-역할 연결 생성
        testAdminRole = new AdminRole();
        testAdminRole.setAdmin(testAdmin);
        testAdminRole.setRole(testRole);

        // 테스트 회원가입 요청 생성
        testSignupRequest = new SignupRequestBody();
        testSignupRequest.setEmail("newadmin@example.com");
        testSignupRequest.setNickname("New Admin");
        testSignupRequest.setPassword("password123!");
        testSignupRequest.setPhoneNumber("010-9876-5432");

        // 테스트 로그인 요청 생성
        testLoginRequest = new LoginReqBody();
        testLoginRequest.setEmail("admin@example.com");
        testLoginRequest.setPassword("password123!");
    }

    // =========================== createAdmin 테스트 (중요도: 높음) ===========================

    @Test
    void createAdmin_성공() {
        // given
        Admin savedAdmin = Admin.builder()
                .id(testAdminId)
                .email("newadmin@example.com")
                .nickname("New Admin")
                .salt("newSalt")
                .hashedPassword("newHashedPassword")
                .phoneNumber("010-9876-5432")
                .status(Status.ACTIVE)
                .build();

        AdminRole savedAdminRole = new AdminRole();
        savedAdminRole.setAdmin(savedAdmin);
        savedAdminRole.setRole(testRole);

        when(adminApiRepository.findByEmail("newadmin@example.com")).thenReturn(Optional.empty());
        when(saltedHashUtil.generateSalt()).thenReturn("newSalt");
        when(saltedHashUtil.hashPassword("password123!", "newSalt")).thenReturn("newHashedPassword");
        when(roleApiRepository.findByDescription("ROLE_ADMIN")).thenReturn(Optional.of(testRole));
        when(adminApiRepository.save(any(Admin.class))).thenAnswer(invocation -> {
            Admin admin = invocation.getArgument(0);
            admin.setId(testAdminId);
            return admin;
        });
        when(adminRoleApiRepository.save(any(AdminRole.class))).thenReturn(savedAdminRole);

        // when
        AuthResponse result = authService.createAdmin(testSignupRequest);

        // then
        assertNotNull(result);
        assertEquals(testAdminId, result.getId());
        assertEquals("newadmin@example.com", result.getEmail());
        assertEquals("New Admin", result.getNickname());
        assertEquals("ACTIVE", result.getStatus());

        verify(adminApiRepository).findByEmail("newadmin@example.com");
        verify(saltedHashUtil).generateSalt();
        verify(saltedHashUtil).hashPassword("password123!", "newSalt");
        verify(roleApiRepository).findByDescription("ROLE_ADMIN");
        verify(adminApiRepository).save(argThat(admin ->
            admin.getEmail().equals("newadmin@example.com") &&
            admin.getNickname().equals("New Admin") &&
            admin.getPhoneNumber().equals("010-9876-5432") &&
            admin.getSalt().equals("newSalt") &&
            admin.getHashedPassword().equals("newHashedPassword") &&
            admin.getStatus() == Status.ACTIVE
        ));
        verify(adminRoleApiRepository).save(any(AdminRole.class));
    }

    @Test
    void createAdmin_실패_중복이메일() {
        // given
        when(adminApiRepository.findByEmail("newadmin@example.com")).thenReturn(Optional.of(testAdmin));
        when(messageUtil.getMessage("auth.user.email.DUPLICATE")).thenReturn("Duplicate email");

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> authService.createAdmin(testSignupRequest));
        assertEquals("Duplicate email", exception.getMessage());

        verify(adminApiRepository, times(1)).findByEmail("newadmin@example.com");
        verify(adminApiRepository, times(0)).save(any(Admin.class));
    }

    @Test
    void createAdmin_실패_역할없음() {
        // given
        when(adminApiRepository.findByEmail("newadmin@example.com")).thenReturn(Optional.empty());
        when(saltedHashUtil.generateSalt()).thenReturn("newSalt");
        when(saltedHashUtil.hashPassword("password123!", "newSalt")).thenReturn("newHashedPassword");
        when(roleApiRepository.findByDescription("ROLE_ADMIN")).thenReturn(Optional.empty());
        when(messageUtil.getMessage("auth.role.NOTFOUND")).thenReturn("Role not found");

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> authService.createAdmin(testSignupRequest));
        assertEquals("Role not found", exception.getMessage());

        verify(roleApiRepository, times(1)).findByDescription("ROLE_ADMIN");
        verify(adminApiRepository, times(0)).save(any(Admin.class));
    }

    // =========================== login 테스트 (중요도: 높음) ===========================

    @Test
    void login_성공() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();
        List<String> roles = Arrays.asList("ROLE_ADMIN");

        when(adminApiRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(testAdmin));
        when(saltedHashUtil.verifyPassword("password123!", "adminSalt", "hashedAdminPassword")).thenReturn(true);
        when(adminApiRepository.findRolesByAdminId(testAdminId)).thenReturn(Arrays.asList(testRole));
        when(jwtUtil.createAccessToken(eq(testAdminId), eq(roles))).thenReturn("accessToken");
        when(jwtUtil.createRefreshToken(eq(testAdminId), eq(roles))).thenReturn("refreshToken");

        // when
        AuthResponse result = authService.login(response, testLoginRequest);

        // then
        assertNotNull(result);
        assertEquals(testAdminId, result.getId());
        assertEquals("admin@example.com", result.getEmail());
        assertEquals("Test Admin", result.getNickname());

        verify(jwtUtil, times(1)).setTokenCookies(eq(response), eq("accessToken"), eq("refreshToken"));
    }

    @Test
    void login_실패_존재하지않는이메일() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(adminApiRepository.findByEmail("admin@example.com")).thenReturn(Optional.empty());
        when(messageUtil.getMessage("auth.admin.email.NOTFOUND")).thenReturn("Admin email not found");

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> authService.login(response, testLoginRequest));
        assertEquals("Admin email not found", exception.getMessage());

        verify(adminApiRepository, times(1)).findByEmail("admin@example.com");
        verify(saltedHashUtil, times(0)).verifyPassword(any(), any(), any());
    }

    @Test
    void login_실패_비활성관리자() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();
        testAdmin.setStatus(Status.INACTIVE);

        when(adminApiRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(testAdmin));
        when(messageUtil.getMessage("auth.admin.INACTIVE", Status.INACTIVE)).thenReturn("Inactive admin");

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> authService.login(response, testLoginRequest));
        assertEquals("Inactive admin", exception.getMessage());

        verify(saltedHashUtil, times(0)).verifyPassword(any(), any(), any());
    }

    @Test
    void login_실패_잘못된비밀번호() {
        // given
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(adminApiRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(testAdmin));
        when(saltedHashUtil.verifyPassword("password123!", "adminSalt", "hashedAdminPassword")).thenReturn(false);
        when(messageUtil.getMessage("auth.admin.password.INCORRECT")).thenReturn("Incorrect password");

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> authService.login(response, testLoginRequest));
        assertEquals("Incorrect password", exception.getMessage());

        verify(saltedHashUtil, times(1)).verifyPassword("password123!", "adminSalt", "hashedAdminPassword");
    }

    // =========================== refreshToken 테스트 (중요도: 높음) ===========================

    @Test
    void refreshToken_성공() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        List<String> roles = Arrays.asList("ROLE_ADMIN");

        Claims claims = new DefaultClaims();
        claims.put("type", "refresh");
        claims.put("userId", testAdminId);

        when(jwtUtil.extractRefreshTokenFromCookie(request)).thenReturn("validRefreshToken");
        when(jwtUtil.validateRefreshToken("validRefreshToken")).thenReturn(true);
        when(jwtUtil.extractRefreshClaims("validRefreshToken")).thenReturn(claims);
        when(adminApiRepository.findById(testAdminId)).thenReturn(Optional.of(testAdmin));
        when(adminApiRepository.findRolesByAdminId(testAdminId)).thenReturn(Arrays.asList(testRole));
        when(jwtUtil.createAccessToken(eq(testAdminId), eq(roles))).thenReturn("newAccessToken");
        when(jwtUtil.createRefreshToken(eq(testAdminId), eq(roles))).thenReturn("newRefreshToken");

        // when
        AuthResponse result = authService.refreshToken(request, response);

        // then
        assertNotNull(result);
        assertEquals(testAdminId, result.getId());
        assertEquals("admin@example.com", result.getEmail());

        verify(jwtUtil, times(1)).setTokenCookies(eq(response), eq("newAccessToken"), eq("newRefreshToken"));
    }

    @Test
    void refreshToken_실패_빈토큰() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.extractRefreshTokenFromCookie(request)).thenReturn("");
        when(messageUtil.getMessage("jwt.EMPTY_TOKEN")).thenReturn("Empty token");

        // when & then
        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.refreshToken(request, response));
        assertEquals("Empty token", exception.getMessage());
    }

    @Test
    void refreshToken_실패_유효하지않은토큰() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtUtil.extractRefreshTokenFromCookie(request)).thenReturn("invalidToken");
        when(jwtUtil.validateRefreshToken("invalidToken")).thenReturn(false);
        when(messageUtil.getMessage("jwt.INVALID_TOKEN")).thenReturn("Invalid token");

        // when & then
        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.refreshToken(request, response));
        assertEquals("Invalid token", exception.getMessage());
    }

    @Test
    void refreshToken_실패_잘못된토큰타입() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Claims claims = new DefaultClaims();
        claims.put("type", "access"); // refresh가 아닌 access 타입
        claims.put("userId", testAdminId);

        when(jwtUtil.extractRefreshTokenFromCookie(request)).thenReturn("accessTypeToken");
        when(jwtUtil.validateRefreshToken("accessTypeToken")).thenReturn(true);
        when(jwtUtil.extractRefreshClaims("accessTypeToken")).thenReturn(claims);
        when(messageUtil.getMessage("jwt.WRONG_TYPE_TOKEN")).thenReturn("Wrong type token");

        // when & then
        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.refreshToken(request, response));
        assertEquals("Wrong type token", exception.getMessage());
    }

    @Test
    void refreshToken_실패_존재하지않는관리자() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Claims claims = new DefaultClaims();
        claims.put("type", "refresh");
        claims.put("userId", testAdminId);

        when(jwtUtil.extractRefreshTokenFromCookie(request)).thenReturn("validRefreshToken");
        when(jwtUtil.validateRefreshToken("validRefreshToken")).thenReturn(true);
        when(jwtUtil.extractRefreshClaims("validRefreshToken")).thenReturn(claims);
        when(adminApiRepository.findById(testAdminId)).thenReturn(Optional.empty());
        when(messageUtil.getMessage("auth.user.email.NOTFOUND")).thenReturn("User not found");

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> authService.refreshToken(request, response));
        assertEquals("User not found", exception.getMessage());
    }

    // =========================== getAdminRoles 테스트 (중요도: 높음) ===========================

    @Test
    void getAdminRoles_성공() {
        // given
        Role adminRole = new Role("ROLE_ADMIN");
        Role superAdminRole = new Role("ROLE_SUPER_ADMIN");
        List<Role> roles = Arrays.asList(adminRole, superAdminRole);

        when(adminApiRepository.findRolesByAdminId(testAdminId)).thenReturn(roles);

        // when
        List<String> result = authService.getAdminRoles(testAdminId);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("ROLE_ADMIN"));
        assertTrue(result.contains("ROLE_SUPER_ADMIN"));

        verify(adminApiRepository, times(1)).findRolesByAdminId(testAdminId);
    }

    @Test
    void getAdminRoles_성공_빈역할목록() {
        // given
        when(adminApiRepository.findRolesByAdminId(testAdminId)).thenReturn(Arrays.asList());

        // when
        List<String> result = authService.getAdminRoles(testAdminId);

        // then
        assertNotNull(result);
        assertEquals(0, result.size());

        verify(adminApiRepository, times(1)).findRolesByAdminId(testAdminId);
    }

    @Test
    void getAdminRoles_성공_단일역할() {
        // given
        List<Role> roles = Arrays.asList(testRole);

        when(adminApiRepository.findRolesByAdminId(testAdminId)).thenReturn(roles);

        // when
        List<String> result = authService.getAdminRoles(testAdminId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ROLE_ADMIN", result.get(0));

        verify(adminApiRepository, times(1)).findRolesByAdminId(testAdminId);
    }
} 