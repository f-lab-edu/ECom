package com.example.core.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("core-test")
class SaltedHashUtilTest {

    @Autowired
    private SaltedHashUtil saltedHashUtil;

    private String testPassword;
    private String testSalt;

    @BeforeEach
    void setUp() {
        testPassword = "testPassword123!";
        testSalt = saltedHashUtil.generateSalt();
    }

    // =========================== generateSalt 테스트 (중요도: 높음) ===========================

    @Test
    void generateSalt_성공_기본동작() {
        // when
        String salt = saltedHashUtil.generateSalt();

        // then
        assertNotNull(salt);
        assertTrue(salt.length() > 0);
        
        // Base64로 인코딩된 16바이트 salt는 24자리 문자열이어야 함 (패딩 포함)
        assertTrue(salt.length() >= 20); // 최소 길이 확인
    }

    @Test
    void generateSalt_성공_유니크성확인() {
        // when
        String salt1 = saltedHashUtil.generateSalt();
        String salt2 = saltedHashUtil.generateSalt();
        String salt3 = saltedHashUtil.generateSalt();

        // then
        assertNotNull(salt1);
        assertNotNull(salt2);
        assertNotNull(salt3);
        
        // 각 salt가 서로 다른지 확인
        assertNotEquals(salt1, salt2);
        assertNotEquals(salt2, salt3);
        assertNotEquals(salt1, salt3);
    }

    @Test
    void generateSalt_성공_Base64인코딩확인() {
        // when
        String salt = saltedHashUtil.generateSalt();

        // then
        assertNotNull(salt);
        
        // Base64 디코딩이 성공하는지 확인
        try {
            byte[] decodedSalt = Base64.getDecoder().decode(salt);
            // 16바이트 salt 확인
            assertTrue(decodedSalt.length == 16);
        } catch (IllegalArgumentException e) {
            // Base64 디코딩 실패시 테스트 실패
            throw new AssertionError("Generated salt is not valid Base64", e);
        }
    }

    @Test
    void generateSalt_성공_연속생성() {
        // given & when
        String[] salts = new String[100];
        for (int i = 0; i < 100; i++) {
            salts[i] = saltedHashUtil.generateSalt();
        }

        // then - 모든 salt가 유니크한지 확인
        for (int i = 0; i < 100; i++) {
            assertNotNull(salts[i]);
            for (int j = i + 1; j < 100; j++) {
                assertNotEquals(salts[i], salts[j], "Salts at index " + i + " and " + j + " are the same");
            }
        }
    }

    // =========================== hashPassword 테스트 (중요도: 높음) ===========================

    @Test
    void hashPassword_성공_기본동작() {
        // when
        String hashedPassword = saltedHashUtil.hashPassword(testPassword, testSalt);

        // then
        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.length() > 0);
        assertNotEquals(testPassword, hashedPassword); // 원본 비밀번호와는 달라야 함
    }

    @Test
    void hashPassword_성공_동일입력동일결과() {
        // when
        String hash1 = saltedHashUtil.hashPassword(testPassword, testSalt);
        String hash2 = saltedHashUtil.hashPassword(testPassword, testSalt);

        // then
        assertNotNull(hash1);
        assertNotNull(hash2);
        assertTrue(hash1.equals(hash2)); // 같은 입력에는 같은 결과
    }

    @Test
    void hashPassword_성공_다른Salt다른결과() {
        // given
        String salt1 = saltedHashUtil.generateSalt();
        String salt2 = saltedHashUtil.generateSalt();

        // when
        String hash1 = saltedHashUtil.hashPassword(testPassword, salt1);
        String hash2 = saltedHashUtil.hashPassword(testPassword, salt2);

        // then
        assertNotNull(hash1);
        assertNotNull(hash2);
        assertNotEquals(hash1, hash2); // 다른 salt면 다른 결과
    }

    @Test
    void hashPassword_성공_다른Password다른결과() {
        // given
        String password1 = "password123";
        String password2 = "password456";

        // when
        String hash1 = saltedHashUtil.hashPassword(password1, testSalt);
        String hash2 = saltedHashUtil.hashPassword(password2, testSalt);

        // then
        assertNotNull(hash1);
        assertNotNull(hash2);
        assertNotEquals(hash1, hash2); // 다른 비밀번호면 다른 결과
    }

    @Test
    void hashPassword_성공_특수문자비밀번호() {
        // given
        String specialPassword = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~";

        // when
        String hashedPassword = saltedHashUtil.hashPassword(specialPassword, testSalt);

        // then
        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.length() > 0);
        assertNotEquals(specialPassword, hashedPassword);
    }

    @Test
    void hashPassword_성공_긴비밀번호() {
        // given
        String longPassword = "ThisIsAVeryLongPasswordThatContainsMoreThan100CharactersAndShouldBeHandledCorrectlyByTheHashingAlgorithmWithoutAnyIssues123!@#";

        // when
        String hashedPassword = saltedHashUtil.hashPassword(longPassword, testSalt);

        // then
        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.length() > 0);
        assertNotEquals(longPassword, hashedPassword);
    }

    @Test
    void hashPassword_성공_빈비밀번호() {
        // given
        String emptyPassword = "";

        // when
        String hashedPassword = saltedHashUtil.hashPassword(emptyPassword, testSalt);

        // then
        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.length() > 0);
    }

    @Test
    void hashPassword_성공_유니코드비밀번호() {
        // given
        String unicodePassword = "비밀번호한글123ＡＢＣ中文🚀";

        // when
        String hashedPassword = saltedHashUtil.hashPassword(unicodePassword, testSalt);

        // then
        assertNotNull(hashedPassword);
        assertTrue(hashedPassword.length() > 0);
        assertNotEquals(unicodePassword, hashedPassword);
    }

    // =========================== verifyPassword 테스트 (중요도: 높음) ===========================

    @Test
    void verifyPassword_성공_정확한비밀번호() {
        // given
        String hashedPassword = saltedHashUtil.hashPassword(testPassword, testSalt);

        // when
        boolean result = saltedHashUtil.verifyPassword(testPassword, testSalt, hashedPassword);

        // then
        assertTrue(result);
    }

    @Test
    void verifyPassword_실패_잘못된비밀번호() {
        // given
        String hashedPassword = saltedHashUtil.hashPassword(testPassword, testSalt);
        String wrongPassword = "wrongPassword123!";

        // when
        boolean result = saltedHashUtil.verifyPassword(wrongPassword, testSalt, hashedPassword);

        // then
        assertFalse(result);
    }

    @Test
    void verifyPassword_실패_잘못된Salt() {
        // given
        String hashedPassword = saltedHashUtil.hashPassword(testPassword, testSalt);
        String wrongSalt = saltedHashUtil.generateSalt();

        // when
        boolean result = saltedHashUtil.verifyPassword(testPassword, wrongSalt, hashedPassword);

        // then
        assertFalse(result);
    }

    @Test
    void verifyPassword_실패_잘못된Hash() {
        // given
        String wrongHash = "wrongHashValue";

        // when
        boolean result = saltedHashUtil.verifyPassword(testPassword, testSalt, wrongHash);

        // then
        assertFalse(result);
    }

    @Test
    void verifyPassword_성공_다양한비밀번호타입() {
        // given
        String[] passwords = {
                "simple123",
                "Complex!Password@2023",
                "한글비밀번호123",
                "!@#$%^&*()",
                "VeryLongPasswordWithManyCharactersIncludingNumbersAndSpecialCharacters12345!@#$%",
                ""
        };

        for (String password : passwords) {
            // when
            String hashedPassword = saltedHashUtil.hashPassword(password, testSalt);
            boolean result = saltedHashUtil.verifyPassword(password, testSalt, hashedPassword);

            // then
            assertTrue(result, "Failed for password: " + password);
        }
    }

    @Test
    void verifyPassword_실패_대소문자구분() {
        // given
        String password = "Password123";
        String hashedPassword = saltedHashUtil.hashPassword(password, testSalt);
        String wrongCasePassword = "password123"; // 대소문자 다름

        // when
        boolean result = saltedHashUtil.verifyPassword(wrongCasePassword, testSalt, hashedPassword);

        // then
        assertFalse(result);
    }

    // =========================== 통합 테스트 ===========================

    @Test
    void 전체흐름_회원가입_로그인시나리오() {
        // given - 회원가입 시나리오
        String userPassword = "userPassword123!";
        String salt = saltedHashUtil.generateSalt();
        String hashedPassword = saltedHashUtil.hashPassword(userPassword, salt);

        // then - 해시된 비밀번호는 원본과 달라야 함
        assertNotNull(salt);
        assertNotNull(hashedPassword);
        assertNotEquals(userPassword, hashedPassword);

        // when - 로그인 시나리오 (정확한 비밀번호)
        boolean loginSuccess = saltedHashUtil.verifyPassword(userPassword, salt, hashedPassword);

        // then
        assertTrue(loginSuccess);

        // when - 로그인 시나리오 (잘못된 비밀번호)
        boolean loginFail = saltedHashUtil.verifyPassword("wrongPassword", salt, hashedPassword);

        // then
        assertFalse(loginFail);
    }

    @Test
    void 다중사용자_비밀번호보안() {
        // given - 여러 사용자가 같은 비밀번호를 사용하는 경우
        String commonPassword = "commonPassword123";
        
        String salt1 = saltedHashUtil.generateSalt();
        String salt2 = saltedHashUtil.generateSalt();
        String salt3 = saltedHashUtil.generateSalt();

        // when
        String hash1 = saltedHashUtil.hashPassword(commonPassword, salt1);
        String hash2 = saltedHashUtil.hashPassword(commonPassword, salt2);
        String hash3 = saltedHashUtil.hashPassword(commonPassword, salt3);

        // then - 같은 비밀번호라도 다른 salt로 인해 다른 해시값을 가져야 함
        assertNotEquals(hash1, hash2);
        assertNotEquals(hash2, hash3);
        assertNotEquals(hash1, hash3);

        // and - 각각 올바른 검증이 되어야 함
        assertTrue(saltedHashUtil.verifyPassword(commonPassword, salt1, hash1));
        assertTrue(saltedHashUtil.verifyPassword(commonPassword, salt2, hash2));
        assertTrue(saltedHashUtil.verifyPassword(commonPassword, salt3, hash3));

        // and - 잘못된 salt로는 검증이 실패해야 함
        assertFalse(saltedHashUtil.verifyPassword(commonPassword, salt1, hash2));
        assertFalse(saltedHashUtil.verifyPassword(commonPassword, salt2, hash3));
        assertFalse(saltedHashUtil.verifyPassword(commonPassword, salt3, hash1));
    }

    @Test
    void 성능_테스트() {
        // given
        String password = "performanceTestPassword123!";
        String salt = saltedHashUtil.generateSalt();

        // when - 해싱 성능 측정
        long startTime = System.currentTimeMillis();
        String hashedPassword = saltedHashUtil.hashPassword(password, salt);
        long hashTime = System.currentTimeMillis() - startTime;

        // when - 검증 성능 측정
        startTime = System.currentTimeMillis();
        boolean verified = saltedHashUtil.verifyPassword(password, salt, hashedPassword);
        long verifyTime = System.currentTimeMillis() - startTime;

        // then
        assertNotNull(hashedPassword);
        assertTrue(verified);
        
        // 일반적으로 해싱과 검증은 1초 이내에 완료되어야 함
        assertTrue(hashTime < 1000, "Hash time too long: " + hashTime + "ms");
        assertTrue(verifyTime < 1000, "Verify time too long: " + verifyTime + "ms");
    }
} 