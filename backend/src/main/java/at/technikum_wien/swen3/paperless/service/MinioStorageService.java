package at.technikum_wien.swen3.paperless.service;

import at.technikum_wien.swen3.paperless.exception.StorageException;
import io.minio.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MinioStorageService {

    private final MinioClient minio;
    private final MinioClient minioPublic;

    public MinioStorageService(
            @Qualifier("minioInternal") MinioClient minio,
            @Qualifier("minioPublic") MinioClient minioPublic) {
        this.minio = minio;
        this.minioPublic = minioPublic;
    }

    @Value("${minio.bucket.name}")
    private String bucketName;

    public String save(MultipartFile file) {
        try {
            String objectName = UUID.randomUUID().toString();
            minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            return objectName;
        } catch (Exception e) {
            log.error("Error while uploading file to MinIO", e);
            throw new StorageException("Error while uploading file to MinIO", e);
        }
    }

    @PostConstruct
    private void createBucketIfNotExists() {
        try {
            boolean found = minio.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minio.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("MinIO bucket '{}' created.", bucketName);
            } else {
                log.info("MinIO bucket '{}' already exists.", bucketName);
            }
        } catch (Exception e) {
            log.error("Error while creating MinIO bucket", e);
            throw new RuntimeException("Error while creating MinIO bucket", e);
        }
    }

    public String getPresignedUrl(String objectName) {
        try {
            return minioPublic.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(1, TimeUnit.MINUTES)
                            .region("us-east-1")
                            .build()
            );
        } catch (Exception e) {
            log.error("Error generating presigned URL for object: {}", objectName, e);
            throw new StorageException("Could not generate download URL", e);
        }
    }
}