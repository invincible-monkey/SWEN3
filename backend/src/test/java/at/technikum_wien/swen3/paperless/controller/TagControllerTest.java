package at.technikum_wien.swen3.paperless.controller;

import at.technikum_wien.swen3.paperless.entity.Tag;
import at.technikum_wien.swen3.paperless.mapper.DocumentSearchMapper;
import at.technikum_wien.swen3.paperless.repository.ElasticSearchRepository;
import at.technikum_wien.swen3.paperless.repository.TagRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.rabbitmq.listener.simple.auto-startup=false"
})
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // --- Mocks ---

    @MockitoBean(name = "minioInternal")
    MinioClient minioInternal;

    @MockitoBean(name = "minioPublic")
    MinioClient minioPublic;

    @MockitoBean
    RabbitTemplate rabbitTemplate;

    @MockitoBean
    ElasticSearchRepository elasticSearchRepository;

    @MockitoBean
    DocumentSearchMapper documentSearchMapper;

    @Test
    void getAllTags_whenTagsExist_thenReturnsList() throws Exception {
        // Arrange
        Tag tag1 = new Tag();
        tag1.setName("invoice");
        tagRepository.save(tag1);

        Tag tag2 = new Tag();
        tag2.setName("contract");
        tagRepository.save(tag2);

        // Act & Assert
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[1].name").exists());
    }

    @Test
    void getAllTags_whenNoTags_thenReturnsEmptyList() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void createTag_whenNewTag_thenCreatesAndReturns() throws Exception {
        // Arrange
        Tag newTag = new Tag();
        newTag.setName("receipt");

        // Act & Assert
        mockMvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newTag)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("receipt"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createTag_whenTagExists_thenReturnsExisting() throws Exception {
        // Arrange - create the tag first
        Tag existingTag = new Tag();
        existingTag.setName("invoice");
        tagRepository.save(existingTag);

        Tag duplicateTag = new Tag();
        duplicateTag.setName("invoice");

        // Act & Assert
        mockMvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateTag)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("invoice"));

        // Verify only one tag exists
        mockMvc.perform(get("/api/tags"))
                .andExpect(jsonPath("$.length()").value(1));
    }
}
