package at.technikum_wien.swen3.paperless.mapper;

import at.technikum_wien.swen3.paperless.dto.DocumentDto;
import at.technikum_wien.swen3.paperless.entity.Document;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DocumentMapper {
    DocumentDto entityToDto(Document document);

    Document dtoToEntity(DocumentDto documentDto);

    List<DocumentDto> entityToDto(List<Document> documents);
}