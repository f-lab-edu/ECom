package com.example.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ActiveProfiles("core-test")
@SpringBootTest
class MessageUtilTest {

    @Autowired
    private MessageUtil messageUtil;

    @MockitoBean
    private MessageSource messageSource;

    @Test
    @DisplayName("메시지 조회 테스트")
    void getMessage() {
        // Given
        String code = "test.code";
        String expectedMessage = "This is a test message.";
        when(messageSource.getMessage(eq(code), any(), any(Locale.class))).thenReturn(expectedMessage);

        // When
        String actualMessage = messageUtil.getMessage(code);

        // Then
        assertEquals(expectedMessage, actualMessage);
    }
} 