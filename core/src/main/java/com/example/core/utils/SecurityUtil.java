package com.example.core.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SecurityUtil {

    /**
     * 현재 인증된 사용자의 ID를 반환합니다.
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * 현재 인증된 사용자가 특정 역할을 가지고 있는지 확인합니다.
     */
    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }

    /**
     * 현재 인증된 사용자가 여러 역할 중 하나라도 가지고 있는지 확인합니다.
     */
    public static boolean hasAnyRole(String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        
        List<String> roleList = List.of(roles);
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> roleList.contains(authority.getAuthority()));
    }

    /**
     * 현재 인증된 사용자의 모든 역할을 반환합니다.
     */
    public static List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return List.of();
        }
        
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    /**
     * 현재 사용자가 인증되었는지 확인합니다.
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 현재 사용자가 리소스의 소유자인지 확인합니다.
     */
    public static boolean isResourceOwner(Long resourceOwnerId) {
        Long currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(resourceOwnerId);
    }

    /**
     * 현재 사용자가 관리자 권한을 가지고 있는지 확인합니다.
     */
    public static boolean isAdmin() {
        return hasAnyRole("ROLE_ADMIN", "ROLE_SUPER_ADMIN");
    }

    /**
     * 현재 사용자가 슈퍼 관리자인지 확인합니다.
     */
    public static boolean isSuperAdmin() {
        return hasRole("ROLE_SUPER_ADMIN");
    }
}