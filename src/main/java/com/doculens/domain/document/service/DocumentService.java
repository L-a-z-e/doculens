package com.doculens.domain.document.service;

import com.doculens.domain.document.dto.DocumentResponse;
import com.doculens.domain.document.entity.SourceType;
import com.doculens.domain.document.repository.DocumentRepository;
import com.doculens.global.error.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentCommandService commandService;

    public DocumentResponse upload(UUID collectionId, MultipartFile file, String title) {
        String safeFilename = sanitizeFilename(file.getOriginalFilename());
        validateFile(file, safeFilename);
        String contentHash = calculateHash(file);

        String documentTitle = (title != null && !title.isBlank()) ? title : safeFilename;
        SourceType sourceType = resolveSourceType(safeFilename);

        return commandService.save(collectionId, documentTitle, sourceType,
                contentHash, file.getSize());
    }

    public Page<DocumentResponse> findByCollectionId(UUID collectionId, Pageable pageable) {
        return documentRepository.findByCollectionId(collectionId, pageable)
                .map(DocumentResponse::from);
    }

    public DocumentResponse findById(UUID id) {
        return DocumentResponse.from(
                documentRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("문서를 찾을 수 없습니다: " + id)));
    }

    public void delete(UUID id) {
        commandService.delete(id);
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
        } catch (NoSuchAlgorithmException | java.io.IOException e) {
            throw new RuntimeException("파일 해시 계산 실패", e);
        }
    }
}
