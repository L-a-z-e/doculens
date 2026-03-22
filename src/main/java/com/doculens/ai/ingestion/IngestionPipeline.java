package com.doculens.ai.ingestion;

import com.doculens.domain.document.entity.Document;
import com.doculens.domain.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class IngestionPipeline {

    private final VectorStore vectorStore;
    private final DocumentRepository documentRepository;

    @Async
    public void process(UUID documentId, Resource fileResource) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalStateException("문서를 찾을 수 없습니다: " + documentId));

        document.startProcessing();
        documentRepository.save(document);

        try {
            List<org.springframework.ai.document.Document> parsed = parse(document, fileResource);
            List<org.springframework.ai.document.Document> chunks = chunk(parsed);
            enrichMetadata(chunks, document);
            vectorStore.add(chunks);

            document.completeProcessing(chunks.size());
            documentRepository.save(document);
            log.info("문서 인덱싱 완료: {} ({}개 청크)", document.getTitle(), chunks.size());

        } catch (Exception e) {
            document.failProcessing(e.getMessage());
            documentRepository.save(document);
            log.error("문서 인덱싱 실패: {}", document.getTitle(), e);
        } finally {
            deleteTempFile(fileResource);
        }
    }

    private void deleteTempFile(Resource resource) {
        try {
            Path path = resource.getFile().toPath();
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("임시 파일 삭제 실패: {}", e.getMessage());
        }
    }

    private List<org.springframework.ai.document.Document> parse(Document document, Resource resource) {
        DocumentReader reader = switch (document.getSourceType()) {
            case PDF -> new PagePdfDocumentReader(resource);
            case MARKDOWN -> new MarkdownDocumentReader(resource, MarkdownDocumentReaderConfig.defaultConfig());
            case TEXT -> new TextReader(resource);
            default -> throw new IllegalArgumentException("지원하지 않는 파일 형식: " + document.getSourceType());
        };
        return reader.read();
    }

    private List<org.springframework.ai.document.Document> chunk(List<org.springframework.ai.document.Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter();
        return splitter.apply(documents);
    }

    private void enrichMetadata(List<org.springframework.ai.document.Document> chunks, Document document) {
        for (int i = 0; i < chunks.size(); i++) {
            chunks.get(i).getMetadata().put("document_id", document.getId().toString());
            chunks.get(i).getMetadata().put("collection_id", document.getCollection().getId().toString());
            chunks.get(i).getMetadata().put("chunk_index", i);
            chunks.get(i).getMetadata().put("title", document.getTitle());
        }
    }
}
