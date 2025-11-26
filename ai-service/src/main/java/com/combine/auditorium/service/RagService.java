package com.combine.auditorium.service;

import com.combine.auditorium.entity.KnowledgeArticle;
import com.combine.auditorium.entity.AIConfig;
import dev.langchain4j.data.segment.TextSegment;

import java.util.List;

public interface RagService {

    void indexArticle(KnowledgeArticle article);

    void deleteArticle(Long id);

    List<TextSegment> retrieve(String query, int topK);

    String buildPromptWithKnowledge(String question, AIConfig config);
}
