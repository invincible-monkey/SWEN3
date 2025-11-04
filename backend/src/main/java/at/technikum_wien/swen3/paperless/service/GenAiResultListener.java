package at.technikum_wien.swen3.paperless.service;

import at.technikum_wien.swen3.paperless.config.RabbitMQConfig;
import at.technikum_wien.swen3.paperless.dto.GenAiResult;
import at.technikum_wien.swen3.paperless.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenAiResultListener {
    private final DocumentRepository documentRepository;

    @RabbitListener(queues = RabbitMQConfig.GENAI_RESULT_QUEUE_NAME)
    public void receiveGenAiResult(GenAiResult result) {
        log.info("Received GenAI result for document ID: {}", result.getDocumentId());

        documentRepository.findById(result.getDocumentId()).ifPresent(document -> {
            document.setStatus(result.getStatus());
            if ("COMPLETED".equals(result.getStatus())) {
                document.setSummary(result.getSummary());
                log.info("Successfully updated summary for document ID: {}", document.getId());
            } else {
                log.error("GenAI summary failed for document ID: {}. Reason: {}", document.getId(), result.getErrorDetails());
            }
            documentRepository.save(document);
        });
    }
}
