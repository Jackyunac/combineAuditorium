package com.combine.auditorium.service.impl;

import com.combine.auditorium.entity.AIConfig;
import com.combine.auditorium.entity.KnowledgeArticle;
import com.combine.auditorium.service.RagService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnBean(EmbeddingStore.class)
@ConditionalOnProperty(name = "ai.local.enabled", havingValue = "true")
@Slf4j
public class RagServiceImpl implements RagService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    @Override
    public void indexArticle(KnowledgeArticle article) {
        try {
            List<TextSegment> segments = splitArticle(article);
            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();
            // Ingestor 接受 Document 列表；将 TextSegment 包装回 Document
            List<Document> docs = new ArrayList<>();
            for (TextSegment seg : segments) {
                docs.add(Document.from(seg.text(), seg.metadata()));
            }
            ingestor.ingest(docs);
        } catch (Exception e) {
            log.error("Failed to index article {}", article.getId(), e);
        }
    }

    @Override
    public void deleteArticle(Long id) {
        log.info("deleteArticle called for id={}, current pgvector store does not support direct delete; consider reindexing", id);
    }

    @Override
    public List<TextSegment> retrieve(String query, int topK) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(topK)
                .build();
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);
        List<TextSegment> segments = new ArrayList<>();
        for (EmbeddingMatch<TextSegment> match : result.matches()) {
            segments.add(match.embedded());
        }
        return segments;
    }

    @Override
    public String buildPromptWithKnowledge(String question, AIConfig config) {
        int limit = config.getKnowledgeLimit() == null || config.getKnowledgeLimit() <= 0 ? 3 : config.getKnowledgeLimit();
        List<TextSegment> docs = retrieve(question, limit);
        StringBuilder context = new StringBuilder();
        docs.forEach(seg -> {
            Metadata md = seg.metadata() == null ? new Metadata() : seg.metadata();
            String title = md.getString("title") == null ? "" : md.getString("title");
            context.append("标题: ").append(title).append("\n").append("内容: ").append(seg.text()).append("\n\n");
        });
        double temp = config.getTemperature() == null ? 0.7 : config.getTemperature();
        int topK = config.getTopK() == null ? 3 : config.getTopK();
        return """
参考以下知识库内容作答；若知识库无法回答请明确说明。遵循参数：temperature=%s, topK=%s。

知识库:
%s

用户问题：%s
""".formatted(temp, topK, context, question);
    }

    private List<TextSegment> splitArticle(KnowledgeArticle article) {
        Metadata metadata = new Metadata();
        metadata.put("id", article.getId() == null ? UUID.randomUUID().toString() : article.getId().toString());
        metadata.put("title", article.getTitle());
        metadata.put("tags", article.getTags());
        metadata.put("source", article.getSource());
        Document doc = Document.from(article.getContent(), metadata);
        return DocumentSplitters.recursive(500, 50).split(doc);
    }
}
