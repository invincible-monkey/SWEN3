package at.technikum_wien.swen3.paperless.controller;

import at.technikum_wien.swen3.paperless.config.RabbitMQConfig;
import at.technikum_wien.swen3.paperless.dto.DocumentDto;
import at.technikum_wien.swen3.paperless.entity.Document;
import at.technikum_wien.swen3.paperless.entity.Tag;
import at.technikum_wien.swen3.paperless.mapper.DocumentMapper;
import at.technikum_wien.swen3.paperless.repository.DocumentRepository;
import at.technikum_wien.swen3.paperless.repository.ElasticSearchRepository;
import at.technikum_wien.swen3.paperless.repository.TagRepository;
import at.technikum_wien.swen3.paperless.search.DocumentSearchEntity;
import at.technikum_wien.swen3.paperless.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private DocumentMapper documentMapper;
    @Autowired
    private ElasticSearchRepository elasticSearchRepository;

    @GetMapping("/search")
    public List<DocumentDto> searchDocuments(@RequestParam String query) {
        // call search method
        List<DocumentSearchEntity> searchResults = elasticSearchRepository.searchByQuery(query);

        // extract IDs
        List<Long> ids = searchResults.stream()
                .map(DocumentSearchEntity::getId)
                .collect(Collectors.toList());

        // fetch full details from DB
        List<Document> docs = documentRepository.findAllById(ids);

        // map to DTOs, so frontend gets exactly what it expects
        return docs.stream()
                .map(documentMapper::entityToDto)
                .collect(Collectors.toList());
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<DocumentDto> createDocument(@RequestParam("title") String title, @RequestParam("file") MultipartFile file) {
        DocumentDto createdDocument = documentService.createDocument(title, file);
        return new ResponseEntity<>(createdDocument, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getDocument(@PathVariable Long id) {
        DocumentDto document = documentService.getDocument(id);
        return ResponseEntity.ok(document);
    }

    @GetMapping
    public ResponseEntity<List<DocumentDto>> getAllDocuments() {
        List<DocumentDto> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocumentDto> updateDocument(@PathVariable Long id, @RequestBody DocumentDto documentDto) {
        DocumentDto updatedDocument = documentService.updateDocument(id, documentDto);
        return ResponseEntity.ok(updatedDocument);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download-url")
    public ResponseEntity<String> getDocumentDownloadUrl(@PathVariable Long id) {
        String url = documentService.getDocumentDownloadUrl(id);
        // Return URL as JSON object
        return ResponseEntity.ok("{\"url\":\"" + url + "\"}");
    }

    @PostMapping("/{id}/tags")
    public ResponseEntity<DocumentDto> addTagToDocument(@PathVariable Long id, @RequestBody Tag tagRequest) {
        return documentRepository.findById(id).map(document -> {
            Tag tag = tagRepository.findByName(tagRequest.getName())
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(tagRequest.getName());
                        return tagRepository.save(newTag);
                    });

            document.getTags().add(tag);
            Document savedDoc = documentRepository.save(document);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.SEARCH_ROUTING_KEY,
                    String.valueOf(savedDoc.getId())
            );

            return ResponseEntity.ok(documentMapper.entityToDto(savedDoc));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/tags/{tagId}")
    public ResponseEntity<DocumentDto> removeTagFromDocument(@PathVariable Long id, @PathVariable Long tagId) {
        return documentRepository.findById(id).map(document -> {

            document.getTags().removeIf(tag -> tag.getId().equals(tagId));

            Document savedDoc = documentRepository.save(document);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.SEARCH_ROUTING_KEY,
                    String.valueOf(savedDoc.getId())
            );

            return ResponseEntity.ok(documentMapper.entityToDto(savedDoc));
        }).orElse(ResponseEntity.notFound().build());
    }
}