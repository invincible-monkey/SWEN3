package at.technikum_wien.swen3.paperless.mapper;

import at.technikum_wien.swen3.paperless.entity.Document;
import at.technikum_wien.swen3.paperless.entity.Tag;
import at.technikum_wien.swen3.paperless.search.DocumentSearchEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DocumentSearchMapper {
    DocumentSearchMapper INSTANCE = Mappers.getMapper(DocumentSearchMapper.class);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "tags", source = "tags")
    DocumentSearchEntity entityToSearchEntity(Document document);

    default List<String> mapTags(Set<Tag> tags) {
        if (tags == null) {
            return Collections.emptyList();
        }
        return tags.stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
    }
}
