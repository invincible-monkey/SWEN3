package at.technikum_wien.swen3.paperless.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MinIOConfig {

    @Value("${minio.url}")
    private String internalUrl;

    @Value("${minio.public.url:${minio.url}}")
    private String publicUrl;

    @Value("${minio.access.key}")
    private String accessKey;

    @Value("${minio.secret.key}")
    private String secretKey;

    @Bean(name = "minioInternal")
    @Primary
    public MinioClient minioInternal() {
        return MinioClient.builder()
                .endpoint(internalUrl)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Bean(name = "minioPublic")
    public MinioClient minioPublic() {
        return MinioClient.builder()
                .endpoint(publicUrl)
                .credentials(accessKey, secretKey)
                .build();
    }
}