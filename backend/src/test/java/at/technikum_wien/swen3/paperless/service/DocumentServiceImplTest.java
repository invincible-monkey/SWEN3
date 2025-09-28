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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentMapper documentMapper;

    @Mock
    private MinioStorageService minioStorageService;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @Test
    void createDocument_whenCalled_thenSavesAndReturnsDto() {
        // Arrange
        String title = "New Test Document";
        MultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test data".getBytes());
        String storagePath = "unique-path-123";
        Document savedDocument = Document.builder().id(1L).title(title).storagePath(storagePath).build();
        DocumentDto expectedDto = DocumentDto.builder().id(1L).title(title).storagePath(storagePath).build();

        when(minioStorageService.save(file)).thenReturn(storagePath);
        when(documentRepository.save(any(Document.class))).thenReturn(savedDocument);
        when(documentMapper.entityToDto(savedDocument)).thenReturn(expectedDto);

        // Act
        DocumentDto result = documentService.createDocument(title, file);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(title);
        assertThat(result.getStoragePath()).isEqualTo(storagePath);
    }

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

    @Test
    void getDocument_whenNotFound_thenThrowException() {
        // Arrange
        long documentId = 99L;
        when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            documentService.getDocument(documentId);
        });
    }

    @Test
    void updateDocument_whenExists_thenReturnsUpdatedDto() {
        // Arrange
        long documentId = 1L;
        Document existingDocument = Document.builder().id(documentId).title("Old Title").build();
        DocumentDto updateRequestDto = DocumentDto.builder().title("New Title").content("New content").build();

        when(documentRepository.findById(documentId)).thenReturn(Optional.of(existingDocument));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return the saved entity
        when(documentMapper.entityToDto(any(Document.class))).thenAnswer(invocation -> {
            Document savedDoc = invocation.getArgument(0);
            return DocumentDto.builder().id(savedDoc.getId()).title(savedDoc.getTitle()).content(savedDoc.getContent()).build();
        });


        // Act
        DocumentDto result = documentService.updateDocument(documentId, updateRequestDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.getContent()).isEqualTo("New content");
    }

    @Test
    void updateDocument_whenNotFound_thenThrowException() {
        // Arrange
        long documentId = 99L;
        DocumentDto updateRequestDto = DocumentDto.builder().title("New Title").build();
        when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            documentService.updateDocument(documentId, updateRequestDto);
        });
    }

    @Test
    void deleteDocument_whenExists_thenDeletesDocument() {
        // Arrange
        long documentId = 1L;
        when(documentRepository.existsById(documentId)).thenReturn(true);
        doNothing().when(documentRepository).deleteById(documentId);

        // Act
        documentService.deleteDocument(documentId);

        // Assert
        verify(documentRepository, times(1)).deleteById(documentId);
    }

    @Test
    void deleteDocument_whenNotFound_thenThrowException() {
        // Arrange
        long documentId = 99L;
        when(documentRepository.existsById(documentId)).thenReturn(false);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            documentService.deleteDocument(documentId);
        });
    }
}