package com.doculens.domain.document.controller;

import com.doculens.domain.document.dto.DocumentResponse;
import com.doculens.domain.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<DocumentResponse> upload(
            @RequestParam MultipartFile file,
            @RequestParam UUID collectionId,
            @RequestParam(required = false) String title) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.upload(collectionId, file, title));
    }

    @GetMapping
    public ResponseEntity<Page<DocumentResponse>> findAll(
            @RequestParam UUID collectionId,
            Pageable pageable) {
        return ResponseEntity.ok(documentService.findByCollectionId(collectionId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(documentService.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        documentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
