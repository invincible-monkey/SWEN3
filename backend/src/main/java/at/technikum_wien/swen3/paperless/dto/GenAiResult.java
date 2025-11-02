package at.technikum_wien.swen3.paperless.dto;

import lombok.Data;

@Data
public class GenAiResult {
    private Long documentId;
    private String summary;
    private String status;
    private String errorDetails;
}
