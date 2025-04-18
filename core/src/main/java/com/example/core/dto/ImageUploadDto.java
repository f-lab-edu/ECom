package com.example.core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadDto {
    String imageId; // UUID = DB id
    String key; // s3 key. 삭제시 사용됨
    String url;
}
