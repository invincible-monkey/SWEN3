package at.technikum_wien.swen3.paperless.search;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Document(indexName = "documents")
public class DocumentSearchEntity {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "english")
    private String content; // OCR

    @Field(type = FieldType.Text, analyzer = "english")
    private String summary; // GenAI

    @Field(type = FieldType.Text)
    private List<String> tags;
}
