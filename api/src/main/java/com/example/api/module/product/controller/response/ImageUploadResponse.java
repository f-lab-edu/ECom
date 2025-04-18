package com.example.api.module.product.controller.response;

import com.example.core.dto.ImageUploadDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageUploadResponse {
    String imageId;
    String key;
    String url;

    public static ImageUploadResponse from(ImageUploadDto dto) {
        return ImageUploadResponse.builder()
                .imageId(dto.getImageId())
                .key(dto.getKey())
                .url(dto.getUrl())
                .build();
    }
}
