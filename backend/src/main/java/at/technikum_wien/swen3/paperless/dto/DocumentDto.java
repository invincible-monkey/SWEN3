package at.technikum_wien.swen3.paperless.dto;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    private Long id;
    private String title;
    private String content;
    private OffsetDateTime createdDate;
    private String storagePath;
    private String status;
    private String summary;
    private long fileSize;
    private List<TagDto> tags;
}