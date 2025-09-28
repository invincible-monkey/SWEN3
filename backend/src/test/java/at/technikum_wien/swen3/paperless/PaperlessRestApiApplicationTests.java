package at.technikum_wien.swen3.paperless;

import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class PaperlessRestApiApplicationTests {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public MinioClient minioClient() {
            return Mockito.mock(MinioClient.class);
        }
    }

    @Test
    void contextLoads() {
    }

}