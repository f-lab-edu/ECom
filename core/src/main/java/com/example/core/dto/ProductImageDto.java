package com.example.core.dto;

import lombok.Data;

@Data
public class ProductImageDto {
    private String imageId;
    private String url;
    private boolean isThumbnail;
    private int imageSortOrder;
}
