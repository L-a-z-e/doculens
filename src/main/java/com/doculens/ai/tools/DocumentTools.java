package com.doculens.ai.tools;

import com.doculens.domain.document.entity.Document;
import com.doculens.domain.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DocumentTools {

    private final VectorStore vectorStore;
    private final DocumentRepository documentRepository;

    @Tool(description = "Search documents in a collection by semantic query. Use this when the user asks about document content.")
    public List<Map<String, String>> searchDocuments(
            @ToolParam(description = "Search query in natural language") String query,
            @ToolParam(description = "Collection ID (UUID format)") String collectionId) {

        return vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(5)
                        .similarityThreshold(0.7)
                        .filterExpression(new FilterExpressionBuilder()
                                .eq("collection_id", collectionId).build())
                        .build()
        ).stream().map(doc -> Map.of(
                "content", doc.getText(),
                "title", doc.getMetadata().getOrDefault("title", "unknown").toString()
        )).toList();
    }

    @Tool(description = "List all documents in a collection with their status. Use this when the user asks what documents exist.")
    public List<Map<String, String>> listDocuments(
            @ToolParam(description = "Collection ID (UUID format)") String collectionId) {

        return documentRepository.findByCollectionId(UUID.fromString(collectionId), Pageable.unpaged())
                .getContent().stream()
                .map(doc -> Map.of(
                        "title", doc.getTitle(),
                        "status", doc.getStatus().name(),
                        "sourceType", doc.getSourceType().name()
                )).toList();
    }
}
