package at.technikum_wien.swen3.paperless.controller;

import at.technikum_wien.swen3.paperless.entity.Document;
import at.technikum_wien.swen3.paperless.mapper.DocumentSearchMapper;
import at.technikum_wien.swen3.paperless.repository.DocumentRepository;
import at.technikum_wien.swen3.paperless.repository.ElasticSearchRepository;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase; // Import this
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.rabbitmq.listener.simple.auto-startup=false"
})
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DocumentRepository documentRepository;

    // --- Mocks ---

    @MockitoBean(name = "minioInternal")
    MinioClient minioInternal;

    @MockitoBean(name = "minioPublic")
    MinioClient minioPublic;

    @MockitoBean
    RabbitTemplate rabbitTemplate;

    @MockitoBean
    ElasticSearchRepository elasticSearchRepository;

    @MockitoBean(name = "elasticsearchTemplate")
    ElasticsearchOperations elasticsearchOperations;

    @MockitoBean
    DocumentSearchMapper documentSearchMapper;

    @Test
    void shouldUploadDocument() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-invoice.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "Dummy PDF Content".getBytes()
        );

        mockMvc.perform(multipart("/api/documents")
                        .file(file)
                        .param("title", "Test Invoice"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Invoice"));
    }

    @Test
    void shouldReturnDocumentList() throws Exception {
        Document doc = new Document();
        doc.setTitle("Seeded Doc");
        doc.setContent("Content");
        doc.setStatus("COMPLETED");
        doc.setStoragePath("bucket/test-path.pdf");

        documentRepository.save(doc);

        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Seeded Doc"));
    }

    @Test
    void shouldReturnNotFoundForInvalidId() throws Exception {
        mockMvc.perform(get("/api/documents/999"))
                .andExpect(status().isNotFound());
    }
}