package com.example.core.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class SaltedHashUtil {

    // SALT 생성
    public String generateSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    // SHA-256을 사용한 비밀번호 해싱
    public String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());  // SALT 추가
            byte[] hashedPassword = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // 비밀번호 검증
    public boolean verifyPassword(String password, String salt, String hashedPassword) {
        return hashPassword(password, salt).equals(hashedPassword);
    }
}
