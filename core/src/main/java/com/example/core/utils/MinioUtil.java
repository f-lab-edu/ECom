package com.example.core.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.core.dto.ImageUploadDto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MinioUtil {

    private final AmazonS3 s3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final String PREFIX = "products/";

    public ImageUploadDto upload(MultipartFile file) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String extention = FilenameUtils.getExtension(file.getOriginalFilename());
        String key = PREFIX + uuid + "." + extention;

        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(file.getSize());
        meta.setContentType(file.getContentType());

        s3.putObject(bucket, key, file.getInputStream(), meta);
        System.out.println("upload success key: " + key);

        String url = s3.getUrl(bucket, key).toString();
        return new ImageUploadDto(uuid, key, url);
    }

    public void delete(String key) {
        s3.deleteObject(bucket, key);
    }
}
