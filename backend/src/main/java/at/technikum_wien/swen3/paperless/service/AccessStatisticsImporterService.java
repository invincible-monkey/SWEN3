package at.technikum_wien.swen3.paperless.service;

import at.technikum_wien.swen3.paperless.config.AccessStatisticsConfig;
import at.technikum_wien.swen3.paperless.dto.AccessEntryXml;
import at.technikum_wien.swen3.paperless.dto.AccessStatisticsXml;
import at.technikum_wien.swen3.paperless.entity.Document;
import at.technikum_wien.swen3.paperless.repository.DocumentRepository;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccessStatisticsImporterService {

    private final DocumentRepository documentRepository;
    private final AccessStatisticsConfig config;

    @Scheduled(cron = "${access-stats.cron-schedule:0 0 1 * * ?}")
    public void importAccessStatistics() {
        log.info("Starting access statistics import job");

        File inputFolder = new File(config.getInputDir());
        if (!inputFolder.exists() || !inputFolder.isDirectory()) {
            log.warn("Access statistics input directory does not exist: {}", config.getInputDir());
            return;
        }

        // Create archive folder if missing
        new File(config.getArchiveDir()).mkdirs();

        Pattern filePattern = Pattern.compile(config.getFilePattern());

        try (Stream<Path> paths = Files.walk(Paths.get(config.getInputDir()), 1)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> filePattern.matcher(path.getFileName().toString()).matches())
                    .forEach(this::processXmlFile);
        } catch (IOException e) {
            log.error("Error reading access statistics input directory", e);
        }

        log.info("Access statistics import job completed");
    }

    @Transactional
    public void processXmlFile(Path filePath) {
        log.info("Processing access statistics file: {}", filePath.getFileName());

        try {
            // Parse the XML file
            JAXBContext context = JAXBContext.newInstance(AccessStatisticsXml.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            AccessStatisticsXml stats = (AccessStatisticsXml) unmarshaller.unmarshal(filePath.toFile());

            if (stats.getEntries() == null || stats.getEntries().isEmpty()) {
                log.warn("No entries found in file: {}", filePath.getFileName());
            } else {
                log.info("Found {} entries in file from date: {}, source: {}",
                        stats.getEntries().size(), stats.getDate(), stats.getSource());

                int successCount = 0;
                int skipCount = 0;

                for (AccessEntryXml entry : stats.getEntries()) {
                    if (updateDocumentAccessCount(entry)) {
                        successCount++;
                    } else {
                        skipCount++;
                    }
                }

                log.info("Processed file {}: {} entries updated, {} entries skipped (document not found)",
                        filePath.getFileName(), successCount, skipCount);
            }

            // Move file to archive
            archiveFile(filePath);

        } catch (JAXBException e) {
            log.error("Failed to parse XML file: {}", filePath, e);
        } catch (Exception e) {
            log.error("Failed to process access statistics file: {}", filePath, e);
        }
    }

    private boolean updateDocumentAccessCount(AccessEntryXml entry) {
        if (entry.getDocumentId() == null || entry.getAccessCount() == null) {
            log.warn("Invalid entry: missing documentId or accessCount");
            return false;
        }

        Optional<Document> documentOpt = documentRepository.findById(entry.getDocumentId());

        if (documentOpt.isEmpty()) {
            log.warn("Document not found with ID: {}", entry.getDocumentId());
            return false;
        }

        Document document = documentOpt.get();
        long newCount = document.getAccessCount() + entry.getAccessCount();
        document.setAccessCount(newCount);
        documentRepository.save(document);

        log.debug("Updated document {} access count: {} -> {}",
                entry.getDocumentId(), document.getAccessCount() - entry.getAccessCount(), newCount);

        return true;
    }

    private void archiveFile(Path filePath) {
        try {
            Path destPath = Paths.get(config.getArchiveDir(), filePath.getFileName().toString());
            Files.move(filePath, destPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Archived file {} to {}", filePath.getFileName(), config.getArchiveDir());
        } catch (IOException e) {
            log.error("Failed to archive file: {}", filePath, e);
        }
    }
}
