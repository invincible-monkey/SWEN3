package at.technikum_wien.swen3.paperless.service;

import at.technikum_wien.swen3.paperless.dto.DocumentDto;
import at.technikum_wien.swen3.paperless.entity.Document;
import at.technikum_wien.swen3.paperless.mapper.DocumentMapper;
import at.technikum_wien.swen3.paperless.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;
    private final MinioStorageService minioStorageService;

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
        // Store the file in MinIO
        String storagePath = minioStorageService.save(file);

        // Create the document metadata
        Document newDoc = new Document();
        newDoc.setTitle(title);
        newDoc.setStoragePath(storagePath);

        // Save metadata to postgres
        Document savedDocument = documentRepository.save(newDoc);
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