package at.technikum_wien.swen3.paperless.service;

import at.technikum_wien.swen3.paperless.exception.StorageException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioStorageServiceTest {

    @Mock
    private MinioClient minioInternal;

    @Mock
    private MinioClient minioPublic;

    private MinioStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new MinioStorageService(minioInternal, minioPublic);
        ReflectionTestUtils.setField(storageService, "bucketName", "test-bucket");
    }

    @Test
    void save_whenValidFile_thenReturnsObjectName() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-document.pdf",
                "application/pdf",
                "PDF content".getBytes());

        // Act
        String result = storageService.save(file);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        // UUID format validation
        assertThat(result).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        verify(minioInternal).putObject(any(PutObjectArgs.class));
    }

    @Test
    void save_whenMinioThrowsException_thenThrowsStorageException() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-document.pdf",
                "application/pdf",
                "PDF content".getBytes());

        when(minioInternal.putObject(any(PutObjectArgs.class)))
                .thenThrow(new RuntimeException("MinIO connection failed"));

        // Act & Assert
        assertThatThrownBy(() -> storageService.save(file))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Error while uploading file to MinIO");
    }

    @Test
    void getPresignedUrl_whenValidObjectName_thenReturnsUrl() throws Exception {
        // Arrange
        String objectName = "test-object-123";
        String expectedUrl = "http://minio:9000/test-bucket/test-object-123?X-Amz-Signature=abc";

        when(minioPublic.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenReturn(expectedUrl);

        // Act
        String result = storageService.getPresignedUrl(objectName);

        // Assert
        assertThat(result).isEqualTo(expectedUrl);
        verify(minioPublic).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @Test
    void getPresignedUrl_whenMinioThrowsException_thenThrowsStorageException() throws Exception {
        // Arrange
        String objectName = "test-object-123";

        when(minioPublic.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenThrow(new RuntimeException("Failed to generate URL"));

        // Act & Assert
        assertThatThrownBy(() -> storageService.getPresignedUrl(objectName))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Could not generate download URL");
    }
}
