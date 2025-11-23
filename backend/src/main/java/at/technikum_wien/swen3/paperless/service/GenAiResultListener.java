package at.technikum_wien.swen3.paperless.service;

import at.technikum_wien.swen3.paperless.config.RabbitMQConfig;
import at.technikum_wien.swen3.paperless.dto.GenAiResult;
import at.technikum_wien.swen3.paperless.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenAiResultListener {
    private final DocumentRepository documentRepository;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.GENAI_RESULT_QUEUE_NAME)
    public void receiveGenAiResult(GenAiResult result) {
        log.info("Received GenAI result for document ID: {}", result.getDocumentId());

        documentRepository.findById(result.getDocumentId()).ifPresent(document -> {
            document.setStatus(result.getStatus());

            if ("COMPLETED".equals(result.getStatus())) {
                document.setSummary(result.getSummary());

                documentRepository.save(document);
                log.info("Successfully updated summary for document ID: {}", document.getId());

                log.info("Sending message for document ID {} to Search Indexing queue (GenAI Summary).", document.getId());
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE_NAME,
                        RabbitMQConfig.SEARCH_ROUTING_KEY,
                        String.valueOf(document.getId())
                );
            } else {
                log.error("GenAI summary failed for document ID: {}. Reason: {}", document.getId(), result.getErrorDetails());
                documentRepository.save(document);
            }
        });
    }
}
