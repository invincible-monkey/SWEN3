package at.technikum_wien.swen3.paperless.service;

import at.technikum_wien.swen3.paperless.config.RabbitMQConfig;
import at.technikum_wien.swen3.paperless.dto.GenAiResult;
import at.technikum_wien.swen3.paperless.entity.Document;
import at.technikum_wien.swen3.paperless.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenAiResultListenerTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private GenAiResultListener genAiResultListener;

    @Test
    void receiveGenAiResult_whenCompleted_thenUpdatesSummaryAndSendsToSearch() {
        // Arrange
        Long documentId = 1L;
        String summary = "This document contains an invoice for office supplies.";

        Document document = Document.builder()
                .id(documentId)
                .title("Test Document")
                .content("OCR extracted content")
                .status("SUCCESS")
                .build();

        GenAiResult result = new GenAiResult();
        result.setDocumentId(documentId);
        result.setStatus("COMPLETED");
        result.setSummary(summary);

        when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        genAiResultListener.receiveGenAiResult(result);

        // Assert
        assertThat(document.getSummary()).isEqualTo(summary);
        assertThat(document.getStatus()).isEqualTo("COMPLETED");
        verify(documentRepository).save(document);

        // Verify message sent to Search queue
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.SEARCH_ROUTING_KEY),
                eq(String.valueOf(documentId)));
    }

    @Test
    void receiveGenAiResult_whenFailed_thenUpdatesStatusOnly() {
        // Arrange
        Long documentId = 1L;

        Document document = Document.builder()
                .id(documentId)
                .title("Test Document")
                .content("OCR content")
                .status("SUCCESS")
                .build();

        GenAiResult result = new GenAiResult();
        result.setDocumentId(documentId);
        result.setStatus("FAILED");
        result.setErrorDetails("GenAI service unavailable");

        when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));

        // Act
        genAiResultListener.receiveGenAiResult(result);

        // Assert
        assertThat(document.getStatus()).isEqualTo("FAILED");
        assertThat(document.getSummary()).isNull(); // Summary should not be set
        verify(documentRepository).save(document);

        // Verify no message sent to Search queue on failure
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void receiveGenAiResult_whenDocumentNotFound_thenDoesNothing() {
        // Arrange
        Long documentId = 999L;

        GenAiResult result = new GenAiResult();
        result.setDocumentId(documentId);
        result.setStatus("COMPLETED");
        result.setSummary("Some summary");

        when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

        // Act
        genAiResultListener.receiveGenAiResult(result);

        // Assert
        verify(documentRepository).findById(documentId);
        verify(documentRepository, never()).save(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }
}
