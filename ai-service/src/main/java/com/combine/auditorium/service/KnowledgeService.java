package com.combine.auditorium.service;

import com.combine.auditorium.entity.KnowledgeArticle;

import java.util.List;

public interface KnowledgeService {

    KnowledgeArticle create(KnowledgeArticle article);

    List<KnowledgeArticle> search(String keyword, String tag, int limit);

    List<KnowledgeArticle> searchTopArticles(String keyword, int limit);

    void delete(Long id);
}
