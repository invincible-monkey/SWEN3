package at.technikum_wien.swen3.paperless.service;

import at.technikum_wien.swen3.paperless.config.RabbitMQConfig;
import at.technikum_wien.swen3.paperless.dto.OcrResult;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OcrResultListenerTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private OcrResultListener ocrResultListener;

    @Test
    void receiveOcrResult_whenSuccess_thenUpdatesDocumentAndSendsMessages() {
        // Arrange
        Long documentId = 1L;
        String ocrContent = "Extracted text from PDF";

        Document document = Document.builder()
                .id(documentId)
                .title("Test Document")
                .status("PROCESSING")
                .build();

        OcrResult result = new OcrResult();
        result.setDocumentId(documentId);
        result.setStatus("SUCCESS");
        result.setContentText(ocrContent);

        when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));
        when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ocrResultListener.receiveOcrResult(result);

        // Assert
        assertThat(document.getContent()).isEqualTo(ocrContent);
        assertThat(document.getStatus()).isEqualTo("SUCCESS");
        verify(documentRepository).save(document);

        // Verify messages sent to GenAI and Search queues
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.GENAI_ROUTING_KEY),
                eq(String.valueOf(documentId)));
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(RabbitMQConfig.SEARCH_ROUTING_KEY),
                eq(String.valueOf(documentId)));
    }

    @Test
    void receiveOcrResult_whenFailure_thenUpdatesStatusOnly() {
        // Arrange
        Long documentId = 1L;

        Document document = Document.builder()
                .id(documentId)
                .title("Test Document")
                .status("PROCESSING")
                .build();

        OcrResult result = new OcrResult();
        result.setDocumentId(documentId);
        result.setStatus("FAILED");
        result.setErrorDetails("OCR engine error");

        when(documentRepository.findById(documentId)).thenReturn(Optional.of(document));

        // Act
        ocrResultListener.receiveOcrResult(result);

        // Assert
        assertThat(document.getStatus()).isEqualTo("FAILED");
        assertThat(document.getContent()).isNull(); // Content should not be set
        verify(documentRepository).save(document);

        // Verify no messages sent to other queues on failure
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }

    @Test
    void receiveOcrResult_whenDocumentNotFound_thenDoesNothing() {
        // Arrange
        Long documentId = 999L;

        OcrResult result = new OcrResult();
        result.setDocumentId(documentId);
        result.setStatus("SUCCESS");
        result.setContentText("Some content");

        when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

        // Act
        ocrResultListener.receiveOcrResult(result);

        // Assert
        verify(documentRepository).findById(documentId);
        verify(documentRepository, never()).save(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), anyString());
    }
}
