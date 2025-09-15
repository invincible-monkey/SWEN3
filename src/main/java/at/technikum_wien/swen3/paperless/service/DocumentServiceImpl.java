package at.technikum_wien.swen3.paperless.service;

import at.technikum_wien.swen3.paperless.dto.DocumentDto;
import at.technikum_wien.swen3.paperless.entity.Document;
import at.technikum_wien.swen3.paperless.mapper.DocumentMapper;
import at.technikum_wien.swen3.paperless.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;

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
    public DocumentDto createDocument(DocumentDto documentDto) {
        Document documentToSave = documentMapper.dtoToEntity(documentDto);
        Document savedDocument = documentRepository.save(documentToSave);
        return documentMapper.entityToDto(savedDocument);
    }

    @Override
    public DocumentDto updateDocument(Long id, DocumentDto documentDto) {
        // First, check if the document exists.
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