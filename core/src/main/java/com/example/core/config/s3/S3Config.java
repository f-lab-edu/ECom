package com.example.core.config.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.s3.endpoint}")
    private String endpoint;

    @Bean
    public AmazonS3 amazons3() {
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(endpoint, "us-east-1"))
//                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance()) // 실제 AWS에서는 DefaultAWSCredentialsProviderChain을 사용하여 IAM Role을 통해 인증을 받는다.
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey))) // Minio 로컬 테스트용.
                .withPathStyleAccessEnabled(true) // MinIO는 AWS S3와 다르게, 경로 기반으로 요청을 받기 때문에 꼭 켜야 하는 설정
                .build();
    }
}
