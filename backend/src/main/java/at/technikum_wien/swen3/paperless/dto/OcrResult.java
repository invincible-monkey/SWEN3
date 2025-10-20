package at.technikum_wien.swen3.paperless.dto;

import lombok.Data;

@Data
public class OcrResult {
    private Long documentId;
    private String contentText;
    private String status;
    private String errorDetails;
}
