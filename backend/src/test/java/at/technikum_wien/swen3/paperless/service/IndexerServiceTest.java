package at.technikum_wien.swen3.paperless.service;

import at.technikum_wien.swen3.paperless.entity.Document;
import at.technikum_wien.swen3.paperless.mapper.DocumentSearchMapper;
import at.technikum_wien.swen3.paperless.repository.DocumentRepository;
import at.technikum_wien.swen3.paperless.repository.ElasticSearchRepository;
import at.technikum_wien.swen3.paperless.search.DocumentSearchEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IndexerServiceTest {
    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private ElasticSearchRepository elasticSearchRepository;

    @Mock
    private DocumentSearchMapper documentSearchMapper;

    @InjectMocks
    private IndexerService indexerService;

    @Test
    void indexDocument_whenDocumentExists_shouldSaveToElastic() {
        // Arrange
        Long docId = 1L;
        String message = "1";
        Document doc = new Document();
        doc.setId(docId);
        doc.setTitle("Test Doc");

        DocumentSearchEntity searchEntity = new DocumentSearchEntity();
        searchEntity.setId(docId);
        searchEntity.setTitle("Test Doc");

        when(documentRepository.findById(docId)).thenReturn(Optional.of(doc));
        when(documentSearchMapper.entityToSearchEntity(doc)).thenReturn(searchEntity);

        // Act
        indexerService.indexDocument(message);

        // Assert
        verify(documentRepository).findById(docId);
        verify(documentSearchMapper).entityToSearchEntity(doc);
        verify(elasticSearchRepository).save(searchEntity); // The crucial check
    }

    @Test
    void indexDocument_whenDocumentDoesNotExist_shouldDoNothing() {
        // Arrange
        Long docId = 99L;
        when(documentRepository.findById(docId)).thenReturn(Optional.empty());

        // Act
        indexerService.indexDocument("99");

        // Assert
        verify(documentRepository).findById(docId);
        verify(elasticSearchRepository, never()).save(any());
    }
}
