package com.combine.auditorium.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * MinIO 文件存储服务
 * 负责与 MinIO 对象存储服务器交互，上传、下载文件
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    /**
     * 上传 MultipartFile 文件 (前端直接上传)
     * 
     * @param file 前端上传的文件对象
     * @return 文件在 MinIO 中的存储路径 (objectName)
     */
    public String upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        // 获取文件后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 生成唯一文件名，避免冲突
        String objectName = UUID.randomUUID().toString() + suffix;

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return objectName;
        } catch (Exception e) {
            log.error("MinIO upload failed", e);
            throw new RuntimeException("File upload failed");
        }
    }
    
    /**
     * 上传本地文件 (用于转码后的文件上传)
     * 
     * @param filePath 本地文件绝对路径
     * @param objectName 目标存储路径
     * @param contentType 文件类型 (MIME)
     * @return 存储路径
     */
    public String uploadLocalFile(String filePath, String objectName, String contentType) {
        try {
             minioClient.uploadObject(
                io.minio.UploadObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .filename(filePath)
                    .contentType(contentType)
                    .build());
             return objectName;
        } catch (Exception e) {
            log.error("MinIO local file upload failed", e);
            throw new RuntimeException("File upload failed");
        }
    }

    /**
     * 获取文件临时访问外链 (带签名)
     * 适用于私有 Bucket，有时效限制
     */
    public String getPresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(60 * 60 * 24) // 24小时有效
                            .build()
            );
        } catch (Exception e) {
            log.error("Get presigned url failed", e);
            return null;
        }
    }
    
    /**
     * 获取永久访问 URL
     * 前提：Bucket 必须设置为 Public Read 权限
     * 格式: endpoint/bucketName/objectName
     */
    public String getPublicUrl(String objectName) {
        return endpoint + "/" + bucketName + "/" + objectName;
    }

    /**
     * 删除单个对象，忽略不存在的错误
     */
    public void deleteObject(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return;
        }
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.warn("MinIO delete failed for object {}", objectName, e);
        }
    }

    /**
     * 删除某个前缀下的所有对象（用于清理 HLS 切片目录）
     */
    public void deletePrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return;
        }
        try {
            List<String> toDelete = new ArrayList<>();
            minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .recursive(true)
                    .prefix(prefix)
                    .build()).forEach(result -> {
                        try {
                            toDelete.add(Objects.requireNonNull(result.get()).objectName());
                        } catch (Exception e) {
                            log.warn("Iterate delete prefix failed for {}", prefix, e);
                        }
                    });
            for (String obj : toDelete) {
                deleteObject(obj);
            }
        } catch (Exception e) {
            log.warn("MinIO deletePrefix failed for {}", prefix, e);
        }
    }

    /**
     * 将公开 URL 转换为对象名，便于删除
     */
    public String toObjectName(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        String prefix = endpoint + "/" + bucketName + "/";
        if (url.startsWith(prefix)) {
            return url.substring(prefix.length());
        }
        return null;
    }

    /**
     * 删除与视频相关的对象：原始文件、封面、转码后的目录
     */
    public void deleteVideoObjects(com.combine.auditorium.entity.Video video) {
        deleteObject(video.getFileId());
        deleteObject(toObjectName(video.getCoverUrl()));
        String videoObject = toObjectName(video.getVideoUrl());
        if (videoObject != null && videoObject.contains("/")) {
            String dir = videoObject.substring(0, videoObject.lastIndexOf('/') + 1);
            deletePrefix(dir);
        }
    }
}
