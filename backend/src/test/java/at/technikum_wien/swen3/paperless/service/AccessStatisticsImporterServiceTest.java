package at.technikum_wien.swen3.paperless.service;

import at.technikum_wien.swen3.paperless.config.AccessStatisticsConfig;
import at.technikum_wien.swen3.paperless.entity.Document;
import at.technikum_wien.swen3.paperless.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessStatisticsImporterServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private AccessStatisticsConfig config;

    @InjectMocks
    private AccessStatisticsImporterService importerService;

    @TempDir
    Path tempDir;

    private Path inputDir;
    private Path archiveDir;

    @BeforeEach
    void setUp() throws IOException {
        inputDir = tempDir.resolve("input");
        archiveDir = tempDir.resolve("archive");
        Files.createDirectories(inputDir);
        Files.createDirectories(archiveDir);

        lenient().when(config.getInputDir()).thenReturn(inputDir.toString());
        lenient().when(config.getArchiveDir()).thenReturn(archiveDir.toString());
        lenient().when(config.getFilePattern()).thenReturn("access-stats-.*\\.xml");
    }

    @Test
    void importAccessStatistics_whenValidXml_thenUpdatesDocumentAndArchivesFile() throws IOException {
        // Arrange
        String xmlContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <accessStatistics date="2025-12-15" source="test-system">
                    <entries>
                        <entry>
                            <documentId>1</documentId>
                            <accessCount>42</accessCount>
                        </entry>
                    </entries>
                </accessStatistics>
                """;

        Path xmlFile = inputDir.resolve("access-stats-2025-12-15.xml");
        Files.writeString(xmlFile, xmlContent);

        Document document = Document.builder()
                .id(1L)
                .title("Test Document")
                .accessCount(10)
                .build();

        when(documentRepository.findById(1L)).thenReturn(Optional.of(document));
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        importerService.importAccessStatistics();

        // Assert
        verify(documentRepository).findById(1L);
        verify(documentRepository).save(any(Document.class));
        assertThat(document.getAccessCount()).isEqualTo(52); // 10 + 42

        // Verify file was archived
        assertThat(Files.exists(xmlFile)).isFalse();
        assertThat(Files.exists(archiveDir.resolve("access-stats-2025-12-15.xml"))).isTrue();
    }

    @Test
    void importAccessStatistics_whenDocumentNotFound_thenSkipsEntryAndArchives() throws IOException {
        // Arrange
        String xmlContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <accessStatistics date="2025-12-15" source="test-system">
                    <entries>
                        <entry>
                            <documentId>999</documentId>
                            <accessCount>100</accessCount>
                        </entry>
                    </entries>
                </accessStatistics>
                """;

        Path xmlFile = inputDir.resolve("access-stats-2025-12-15.xml");
        Files.writeString(xmlFile, xmlContent);

        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        importerService.importAccessStatistics();

        // Assert
        verify(documentRepository).findById(999L);
        verify(documentRepository, never()).save(any());

        // File should still be archived
        assertThat(Files.exists(archiveDir.resolve("access-stats-2025-12-15.xml"))).isTrue();
    }

    @Test
    void importAccessStatistics_whenNoMatchingFiles_thenDoesNothing() throws IOException {
        // Arrange - create a file that doesn't match the pattern
        Path nonMatchingFile = inputDir.resolve("other-file.txt");
        Files.writeString(nonMatchingFile, "not an xml file");

        // Act
        importerService.importAccessStatistics();

        // Assert
        verifyNoInteractions(documentRepository);
        // Non-matching file should still exist
        assertThat(Files.exists(nonMatchingFile)).isTrue();
    }

    @Test
    void importAccessStatistics_whenInputDirDoesNotExist_thenReturnsEarly() {
        // Arrange
        when(config.getInputDir()).thenReturn("/nonexistent/path");

        // Act
        importerService.importAccessStatistics();

        // Assert
        verifyNoInteractions(documentRepository);
    }

    @Test
    void importAccessStatistics_whenMultipleEntries_thenUpdatesAll() throws IOException {
        // Arrange
        String xmlContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <accessStatistics date="2025-12-15" source="test-system">
                    <entries>
                        <entry>
                            <documentId>1</documentId>
                            <accessCount>10</accessCount>
                        </entry>
                        <entry>
                            <documentId>2</documentId>
                            <accessCount>20</accessCount>
                        </entry>
                    </entries>
                </accessStatistics>
                """;

        Path xmlFile = inputDir.resolve("access-stats-2025-12-15.xml");
        Files.writeString(xmlFile, xmlContent);

        Document doc1 = Document.builder().id(1L).title("Doc 1").accessCount(0).build();
        Document doc2 = Document.builder().id(2L).title("Doc 2").accessCount(5).build();

        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc1));
        when(documentRepository.findById(2L)).thenReturn(Optional.of(doc2));
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        importerService.importAccessStatistics();

        // Assert
        assertThat(doc1.getAccessCount()).isEqualTo(10);
        assertThat(doc2.getAccessCount()).isEqualTo(25); // 5 + 20
        verify(documentRepository, times(2)).save(any(Document.class));
    }
}
