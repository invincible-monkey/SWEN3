package at.technikum_wien.swen3.paperless.service;

import at.technikum_wien.swen3.paperless.config.RabbitMQConfig;
import at.technikum_wien.swen3.paperless.dto.OcrResult;
import at.technikum_wien.swen3.paperless.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OcrResultListener {
    private final DocumentRepository documentRepository;

    @RabbitListener(queues = RabbitMQConfig.OCR_RESULT_QUEUE_NAME)
    public void receiveOcrResult(OcrResult result) {
        log.info("Received OCR result for document ID: {}", result.getDocumentId());

        documentRepository.findById(result.getDocumentId()).ifPresent(document -> {
            document.setStatus(result.getStatus());
            if ("SUCCESS".equals(result.getStatus())) {
                document.setContent(result.getContentText());
                log.info("Successfully updated content for document ID: {}", document.getId());
            } else {
                log.error("OCR failed for document ID: {}. Reason: {}", document.getId(), result.getErrorDetails());
            }
            documentRepository.save(document);
        });
    }
}
