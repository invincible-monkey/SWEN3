package at.technikum_wien.swen3.paperless.service;

import at.technikum_wien.swen3.paperless.config.RabbitMQConfig;
import at.technikum_wien.swen3.paperless.entity.Document;
import at.technikum_wien.swen3.paperless.mapper.DocumentSearchMapper;
import at.technikum_wien.swen3.paperless.repository.DocumentRepository;
import at.technikum_wien.swen3.paperless.repository.ElasticSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexerService {
    private final DocumentRepository documentRepository;
    private final ElasticSearchRepository elasticSearchRepository;
    private final DocumentSearchMapper documentSearchMapper;

    @RabbitListener(queues = RabbitMQConfig.SEARCH_QUEUE_NAME)
    public void indexDocument(String documentId) {
        log.info("Received indexing request for document ID: {}", documentId);

        try {
            Long id = Long.parseLong(documentId);
            Document document = documentRepository.findById(id).orElse(null);

            if (document == null) {
                log.warn("Document with ID {} not found in database. Skipping indexing.", id);
                return;
            }

            // Map DB entity to Search entity
            var searchEntity = documentSearchMapper.entityToSearchEntity(document);

            // Save to Elasticsearch
            elasticSearchRepository.save(searchEntity);
            log.info("Successfully indexed document ID: {} into Elasticsearch.", id);

        } catch (NumberFormatException e) {
            log.error("Invalid document ID received for indexing: {}", documentId);
        } catch (Exception e) {
            log.error("Error occurred while indexing document ID: {}", documentId, e);
        }
    }
}
