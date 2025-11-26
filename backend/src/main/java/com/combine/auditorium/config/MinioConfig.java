package com.combine.auditorium.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * 应用启动时初始化 Bucket
     * 使用 CommandLineRunner 在所有 Bean 加载完成后执行
     */
    @Bean
    public CommandLineRunner initBucket(MinioClient minioClient) {
        return args -> {
            try {
                boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
                if (!found) {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                    
                    // 设置 Bucket 为 Public (只读)
                    String policy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::" + bucketName + "/*\"]}]}";
                    minioClient.setBucketPolicy(
                            SetBucketPolicyArgs.builder().bucket(bucketName).config(policy).build()
                    );
                    log.info("MinIO bucket '{}' created and policy set to public.", bucketName);
                } else {
                     log.info("MinIO bucket '{}' already exists.", bucketName);
                }
            } catch (Exception e) {
                log.error("Error initializing MinIO bucket", e);
            }
        };
    }
}
