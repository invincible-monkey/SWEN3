package at.technikum_wien.swen3.paperless.mapper;

import at.technikum_wien.swen3.paperless.entity.Document;
import at.technikum_wien.swen3.paperless.entity.Tag;
import at.technikum_wien.swen3.paperless.search.DocumentSearchEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentSearchMapperTest {

    private final DocumentSearchMapper mapper = Mappers.getMapper(DocumentSearchMapper.class);

    @Test
    void entityToSearchEntity_whenValidDocument_thenMapsAllFields() {
        // Arrange
        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setName("invoice");

        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setName("finance");

        Set<Tag> tags = new HashSet<>();
        tags.add(tag1);
        tags.add(tag2);

        Document document = Document.builder()
                .id(1L)
                .title("Test Document")
                .content("Test content from OCR")
                .summary("AI generated summary")
                .tags(tags)
                .build();

        // Act
        DocumentSearchEntity result = mapper.entityToSearchEntity(document);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Document");
        assertThat(result.getContent()).isEqualTo("Test content from OCR");
        assertThat(result.getSummary()).isEqualTo("AI generated summary");
        assertThat(result.getTags()).hasSize(2);
        assertThat(result.getTags()).containsExactlyInAnyOrder("invoice", "finance");
    }

    @Test
    void mapTags_whenTagsNull_thenReturnsEmptyList() {
        // Act
        List<String> result = mapper.mapTags(null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void mapTags_whenTagsPresent_thenReturnsTagNames() {
        // Arrange
        Tag tag1 = new Tag();
        tag1.setId(1L);
        tag1.setName("invoice");

        Tag tag2 = new Tag();
        tag2.setId(2L);
        tag2.setName("contract");

        Set<Tag> tags = new HashSet<>();
        tags.add(tag1);
        tags.add(tag2);

        // Act
        List<String> result = mapper.mapTags(tags);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder("invoice", "contract");
    }

    @Test
    void mapTags_whenEmptyTags_thenReturnsEmptyList() {
        // Arrange
        Set<Tag> tags = Collections.emptySet();

        // Act
        List<String> result = mapper.mapTags(tags);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }
}
