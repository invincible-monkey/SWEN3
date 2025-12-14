package at.technikum_wien.swen3.paperless.service;

import at.technikum_wien.swen3.paperless.util.SimpleMultipartFile; // Import your new class
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImporterService {

    private final DocumentService documentService;

    private final String IMPORT_DIR = "/import";
    private final String PROCESSED_DIR = "/import/processed";

    @Scheduled(fixedRate = 10000) // Run every 10 seconds
    public void importDocuments() {
        File importFolder = new File(IMPORT_DIR);
        if (!importFolder.exists() || !importFolder.isDirectory()) {
            return;
        }

        // Create processed folder if missing
        new File(PROCESSED_DIR).mkdirs();

        try (Stream<Path> paths = Files.walk(Paths.get(IMPORT_DIR), 1)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".pdf"))
                    .forEach(this::processFile);
        } catch (IOException e) {
            log.error("Error reading import directory", e);
        }
    }

    private void processFile(Path filePath) {
        log.info("Found file in import folder: {}", filePath.getFileName());
        try {
            File file = filePath.toFile();

            // Read the file content into a byte array
            byte[] content = Files.readAllBytes(filePath);

            MultipartFile multipartFile = new SimpleMultipartFile(
                    "file",
                    file.getName(),
                    "application/pdf",
                    content
            );

            documentService.createDocument(file.getName().replace(".pdf", ""), multipartFile);

            Path destPath = Paths.get(PROCESSED_DIR, file.getName());
            Files.move(filePath, destPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("Successfully imported and moved: {}", file.getName());

        } catch (Exception e) {
            log.error("Failed to import file: {}", filePath, e);
        }
    }
}