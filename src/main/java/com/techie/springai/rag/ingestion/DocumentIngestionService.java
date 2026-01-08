package com.techie.springai.rag.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);
    private final VectorStore vectorStore;

    public DocumentIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void ingestPdf(MultipartFile file) throws IOException {
        try {
            log.info("Reading file bytes for: {}", file.getOriginalFilename());
            byte[] fileBytes = file.getBytes();
            log.info("Read {} bytes from file", fileBytes.length);
            
            log.info("Creating TikaDocumentReader for file: {}", file.getOriginalFilename());
            ByteArrayResource resource = new ByteArrayResource(fileBytes);
            TikaDocumentReader reader = new TikaDocumentReader(resource);
            
            log.info("Reading documents from PDF");
            var documents = reader.read();
            log.info("Read {} documents from PDF", documents.size());
            
            TextSplitter textSplitter = new TokenTextSplitter();
            log.info("Splitting documents into chunks");
            var splitDocuments = textSplitter.split(documents);
            log.info("Split into {} chunks", splitDocuments.size());
            
            log.info("Storing documents in vector store");
            vectorStore.accept(splitDocuments);
            log.info("Completed ingesting PDF file: {}", file.getOriginalFilename());
        } catch (Exception e) {
            log.error("Error during PDF ingestion for file: {}", file.getOriginalFilename(), e);
            throw new IOException("Failed to ingest PDF: " + e.getMessage(), e);
        }
    }
}
