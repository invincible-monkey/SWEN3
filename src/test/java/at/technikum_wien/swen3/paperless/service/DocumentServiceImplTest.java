package at.technikum_wien.swen3.paperless.service;

import at.technikum_wien.swen3.paperless.dto.DocumentDto;
import at.technikum_wien.swen3.paperless.entity.Document;
import at.technikum_wien.swen3.paperless.mapper.DocumentMapper;
import at.technikum_wien.swen3.paperless.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentMapper documentMapper;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @Test
    void getDocument_whenExists_thenReturnDocumentDto() {
        // Arrange: Set up the test data and mock behavior
        Document document = Document.builder()
                .id(1L)
                .title("Test Document")
                .content("Test content")
                .createdDate(OffsetDateTime.now())
                .build();

        DocumentDto documentDto = DocumentDto.builder()
                .id(1L)
                .title("Test Document")
                .build();

        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(documentMapper.entityToDto(document)).thenReturn(documentDto);

        // Act: Call the method we are testing
        DocumentDto result = documentService.getDocument(1L);

        // Assert: Verify the result is as expected
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Test Document");
    }
}