package at.technikum_wien.swen3.paperless.controller;

import at.technikum_wien.swen3.paperless.exception.StorageException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleStorageException_thenReturns503() {
        // Arrange
        StorageException exception = new StorageException("MinIO connection failed", new RuntimeException());

        // Act
        ResponseEntity<String> response = handler.handleStorageException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isEqualTo("File storage service is currently unavailable.");
    }

    @Test
    void handleIllegalArgumentException_thenReturns400() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid document title");

        // Act
        ResponseEntity<String> response = handler.handleIllegalArgumentException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("Invalid document title");
    }

    @Test
    void handleNotFound_thenReturns404() {
        // Arrange
        NoSuchElementException exception = new NoSuchElementException("Document with ID 123 not found");

        // Act
        ResponseEntity<String> response = handler.handleNotFound(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo("Document with ID 123 not found");
    }
}
