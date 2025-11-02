package at.technikum_wien.swen3.paperless.service;

import at.technikum_wien.swen3.paperless.config.RabbitMQConfig;
import at.technikum_wien.swen3.paperless.dto.DocumentDto;
import at.technikum_wien.swen3.paperless.entity.Document;
import at.technikum_wien.swen3.paperless.mapper.DocumentMapper;
import at.technikum_wien.swen3.paperless.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final MinioStorageService minioStorageService;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public DocumentDto getDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Document not found with id: " + id));
        return documentMapper.entityToDto(document);
    }

    @Override
    public List<DocumentDto> getAllDocuments() {
        List<Document> documents = documentRepository.findAll();
        return documentMapper.entityToDto(documents);
    }

    @Override
    @Transactional
    public DocumentDto createDocument(String title, MultipartFile file) {
        if (file.isEmpty() || !Objects.equals(file.getContentType(), "application/pdf")) {
            log.warn("Attempted to upload invalid file: {}", file.getOriginalFilename());
            throw new IllegalArgumentException("Invalid file: Please upload a PDF document.");
        }

        log.info("Beginning file upload process for document title: {}", title);

        // Store the file in MinIO
        String storagePath = minioStorageService.save(file);
        log.info("File successfully stored in MinIO with path: {}", storagePath);

        // Create the document metadata
        Document newDoc = new Document();
        newDoc.setTitle(title);
        newDoc.setStoragePath(storagePath);
        newDoc.setStatus("PROCESSING");
        newDoc.setFileSize(file.getSize());

        // Save metadata to postgres
        Document savedDocument = documentRepository.save(newDoc);
        log.info("Document metadata saved to database with ID: {}", savedDocument.getId());

        log.info("Sending message for document ID {} to RabbitMQ.", savedDocument.getId());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.ROUTING_KEY, String.valueOf(savedDocument.getId()));

        return documentMapper.entityToDto(savedDocument);
    }

    @Override
    public DocumentDto updateDocument(Long id, DocumentDto documentDto) {
        // Check if the document exists.
        Document existingDocument = documentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Document not found with id: " + id));

        // Update the fields
        existingDocument.setTitle(documentDto.getTitle());
        existingDocument.setContent(documentDto.getContent());

        // Save the updated document and return its DTO
        Document updatedDocument = documentRepository.save(existingDocument);
        return documentMapper.entityToDto(updatedDocument);
    }

    @Override
    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new NoSuchElementException("Document not found with id: " + id);
        }
        documentRepository.deleteById(id);
    }
}