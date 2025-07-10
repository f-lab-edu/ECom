package com.example.core.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.core.dto.ImageUploadDto;

@ActiveProfiles("core-test")
@SpringBootTest
class MinioUtilTest {

    @Autowired
    private MinioUtil minioUtil;

    @MockitoBean
    private AmazonS3 amazonS3;

    private MockMultipartFile testFile;

    @BeforeEach
    void setUp() {
        Mockito.reset(amazonS3);
        testFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test data".getBytes()
        );
    }

    @Test
    @DisplayName("파일 업로드 성공")
    void upload_success() throws Exception {
        // given
        when(amazonS3.putObject(anyString(), anyString(), any(), any(ObjectMetadata.class))).thenReturn(null);
        when(amazonS3.getUrl(anyString(), anyString())).thenReturn(new URL("http://test.com/products/some-uuid.jpg"));

        // when
        ImageUploadDto result = minioUtil.upload(testFile, "products");

        // then
        assertNotNull(result);
        assertNotNull(result.getImageId());
        assertTrue(result.getUrl().contains("products/"));
        verify(amazonS3, times(1)).putObject(anyString(), anyString(), any(), any(ObjectMetadata.class));
    }

    @Test
    @DisplayName("파일 삭제 성공")
    void delete_success() {
        // given
        String key = "products/some-image.jpg";
        doNothing().when(amazonS3).deleteObject(anyString(), anyString());

        // when
        minioUtil.delete(key);

        // then
        verify(amazonS3, times(1)).deleteObject(anyString(), anyString());
    }

    @Test
    @DisplayName("S3Exception 발생 시 업로드 실패")
    void upload_fail_on_s3_exception() {
        // given
        when(amazonS3.putObject(anyString(), anyString(), any(), any(ObjectMetadata.class)))
                .thenThrow(new AmazonS3Exception("Upload failed"));

        // when & then
        assertThrows(RuntimeException.class, () -> minioUtil.upload(testFile, "products"));
    }
} 