package at.technikum_wien.swen3.paperless.service;

import at.technikum_wien.swen3.paperless.dto.DocumentDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {
    DocumentDto getDocument(Long id);

    List<DocumentDto> getAllDocuments();

    DocumentDto createDocument(String title, MultipartFile file);

    DocumentDto updateDocument(Long id, DocumentDto documentDto);

    void deleteDocument(Long id);

    String getDocumentDownloadUrl(Long id);
}