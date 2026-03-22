package com.doculens.domain.document.service;

import com.doculens.ai.ingestion.IngestionPipeline;
import com.doculens.domain.document.dto.DocumentResponse;
import com.doculens.domain.document.entity.SourceType;
import com.doculens.domain.document.repository.DocumentRepository;
import com.doculens.global.error.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentCommandService commandService;
    private final IngestionPipeline ingestionPipeline;

    public DocumentResponse upload(UUID collectionId, MultipartFile file, String title) {
        String safeFilename = sanitizeFilename(file.getOriginalFilename());
        validateFile(file, safeFilename);
        String contentHash = calculateHash(file);

        String documentTitle = (title != null && !title.isBlank()) ? title : safeFilename;
        SourceType sourceType = resolveSourceType(safeFilename);

        DocumentResponse response = commandService.save(collectionId, documentTitle, sourceType,
                contentHash, file.getSize());

        // 임시 파일로 복사 후 비동기 파이프라인 실행
        Path tempFile = copyToTempFile(file, response.id());
        ingestionPipeline.process(response.id(), new FileSystemResource(tempFile));

        return response;
    }

    @Transactional(readOnly = true)
    public Page<DocumentResponse> findByCollectionId(UUID collectionId, Pageable pageable) {
        return documentRepository.findByCollectionId(collectionId, pageable)
                .map(DocumentResponse::from);
    }

    @Transactional(readOnly = true)
    public DocumentResponse findById(UUID id) {
        return DocumentResponse.from(
                documentRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("문서를 찾을 수 없습니다: " + id)));
    }

    public void delete(UUID id) {
        commandService.delete(id);
    }

    private Path copyToTempFile(MultipartFile file, UUID documentId) {
        try {
            Path tempDir = Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir"), "doculens"));
            Path tempFile = tempDir.resolve(documentId.toString());
            file.transferTo(tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("임시 파일 복사 실패", e);
        }
    }

    private void validateFile(MultipartFile file, String filename) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 10MB 이하여야 합니다");
        }
        if (!filename.toLowerCase().matches(".*\\.(pdf|md|txt)$")) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다 (PDF, MD, TXT만 가능)");
        }
    }

    private String sanitizeFilename(String filename) {
        if (filename == null) return "unknown";
        String cleaned = filename.replace("\\", "/");
        int lastSlash = cleaned.lastIndexOf('/');
        if (lastSlash >= 0) {
            cleaned = cleaned.substring(lastSlash + 1);
        }
        if (cleaned.contains("..")) {
            throw new IllegalArgumentException("잘못된 파일명입니다");
        }
        return cleaned.isBlank() ? "unknown" : cleaned;
    }

    private SourceType resolveSourceType(String filename) {
        if (filename == null) return SourceType.PDF;
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return SourceType.PDF;
        if (lower.endsWith(".md")) return SourceType.MARKDOWN;
        if (lower.endsWith(".txt")) return SourceType.TEXT;
        return SourceType.PDF;
    }

    private String calculateHash(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (var is = file.getInputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("파일 해시 계산 실패", e);
        }
    }
}
