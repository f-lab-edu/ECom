package com.example.api.module.product.controller.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImageUploadRequest {
    private String title;
    private String url;
    private MultipartFile file;
}
