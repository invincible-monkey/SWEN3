package at.technikum_wien.swen3.paperless.mapper;

import at.technikum_wien.swen3.paperless.dto.DocumentDto;
import at.technikum_wien.swen3.paperless.entity.Document;
import at.technikum_wien.swen3.paperless.entity.Tag;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentMapperTest {

    private final DocumentMapper mapper = Mappers.getMapper(DocumentMapper.class);

    @Test
    void entityToDto_whenValidDocument_thenMapsAllFields() {
        // Arrange
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("invoice");

        Set<Tag> tags = new HashSet<>();
        tags.add(tag);

        Document document = Document.builder()
                .id(1L)
                .title("Test Document")
                .content("Test content")
                .createdDate(OffsetDateTime.now())
                .storagePath("bucket/path/file.pdf")
                .status("COMPLETED")
                .summary("Test summary")
                .fileSize(1024L)
                .accessCount(5L)
                .tags(tags)
                .build();

        // Act
        DocumentDto result = mapper.entityToDto(document);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Document");
        assertThat(result.getContent()).isEqualTo("Test content");
        assertThat(result.getStoragePath()).isEqualTo("bucket/path/file.pdf");
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(result.getSummary()).isEqualTo("Test summary");
        assertThat(result.getFileSize()).isEqualTo(1024L);
        assertThat(result.getAccessCount()).isEqualTo(5L);
        assertThat(result.getTags()).hasSize(1);
    }

    @Test
    void entityToDto_whenNullDocument_thenReturnsNull() {
        // Act
        DocumentDto result = mapper.entityToDto((Document) null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void dtoToEntity_whenValidDto_thenMapsAllFields() {
        // Arrange
        DocumentDto dto = DocumentDto.builder()
                .id(1L)
                .title("Test Document")
                .content("Test content")
                .storagePath("bucket/path/file.pdf")
                .status("PENDING")
                .summary("Test summary")
                .fileSize(2048L)
                .accessCount(10L)
                .build();

        // Act
        Document result = mapper.dtoToEntity(dto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Test Document");
        assertThat(result.getContent()).isEqualTo("Test content");
        assertThat(result.getStoragePath()).isEqualTo("bucket/path/file.pdf");
        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getSummary()).isEqualTo("Test summary");
        assertThat(result.getFileSize()).isEqualTo(2048L);
        assertThat(result.getAccessCount()).isEqualTo(10L);
    }

    @Test
    void entityToDto_whenList_thenMapsAllDocuments() {
        // Arrange
        Document doc1 = Document.builder()
                .id(1L)
                .title("Document 1")
                .content("Content 1")
                .storagePath("path1")
                .status("COMPLETED")
                .build();

        Document doc2 = Document.builder()
                .id(2L)
                .title("Document 2")
                .content("Content 2")
                .storagePath("path2")
                .status("PENDING")
                .build();

        List<Document> documents = List.of(doc1, doc2);

        // Act
        List<DocumentDto> result = mapper.entityToDto(documents);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Document 1");
        assertThat(result.get(1).getTitle()).isEqualTo("Document 2");
    }
}
