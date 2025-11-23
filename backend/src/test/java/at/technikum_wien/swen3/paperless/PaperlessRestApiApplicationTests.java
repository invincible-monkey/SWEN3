package at.technikum_wien.swen3.paperless;

import at.technikum_wien.swen3.paperless.mapper.DocumentSearchMapper;
import at.technikum_wien.swen3.paperless.repository.ElasticSearchRepository;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.rabbitmq.listener.simple.auto-startup=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class PaperlessRestApiApplicationTests {

    // --- MinIO ---
    @MockitoBean(name = "minioInternal")
    MinioClient minioInternal;

    @MockitoBean(name = "minioPublic")
    MinioClient minioPublic;

    // --- Elasticsearch ---
    @MockitoBean
    ElasticSearchRepository elasticSearchRepository;

    // --- RabbitMQ ---
    @MockitoBean
    RabbitTemplate rabbitTemplate;

    // --- Mapper ---
    @MockitoBean
    DocumentSearchMapper documentSearchMapper;

    @Test
    void contextLoads() {
    }
}
