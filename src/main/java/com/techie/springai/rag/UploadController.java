package com.techie.springai.rag;

import com.techie.springai.rag.ingestion.DocumentIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class UploadController {

    private static final Logger log = LoggerFactory.getLogger(UploadController.class);
    private final DocumentIngestionService documentIngestionService;

    public UploadController(DocumentIngestionService documentIngestionService) {
        this.documentIngestionService = documentIngestionService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        // Check if it's a PDF by content type or file extension
        boolean isPdf = (contentType != null && contentType.equals("application/pdf")) ||
                       (filename != null && filename.toLowerCase().endsWith(".pdf"));
        
        if (!isPdf) {
            return ResponseEntity.badRequest().body("File must be a PDF");
        }

        try {
            log.info("Starting PDF upload: {}", filename);
            documentIngestionService.ingestPdf(file);
            log.info("Successfully uploaded and ingested PDF: {}", filename);
            return ResponseEntity.ok("PDF uploaded and ingested successfully");
        } catch (Exception e) {
            log.error("Error processing PDF: {}", filename, e);
            return ResponseEntity.internalServerError()
                    .body("Error processing PDF: " + e.getMessage() + 
                          (e.getCause() != null ? " (Cause: " + e.getCause().getMessage() + ")" : ""));
        }
    }
}

