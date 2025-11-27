package com.combine.auditorium.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.combine.auditorium.entity.KnowledgeArticle;
import com.combine.auditorium.mapper.KnowledgeArticleMapper;
import com.combine.auditorium.service.KnowledgeService;
import com.combine.auditorium.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {

    private static final int MAX_LIMIT = 100;
    private final KnowledgeArticleMapper knowledgeArticleMapper;
    private final Optional<RagService> ragService;

    @Override
    public KnowledgeArticle create(KnowledgeArticle article) {
        if (article.getTitle() == null || article.getTitle().trim().isEmpty()) {
            throw new RuntimeException("标题不能为空");
        }
        if (article.getContent() == null || article.getContent().trim().isEmpty()) {
            throw new RuntimeException("内容不能为空");
        }
        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());
        knowledgeArticleMapper.insert(article);
        // 索引到向量库（可选）
        try {
            ragService.ifPresent(r -> r.indexArticle(article));
        } catch (Exception e) {
            // 吃掉异常避免影响主流程
        }
        return article;
    }

    @Override
    public List<KnowledgeArticle> search(String keyword, String tag, int limit) {
        int rows = normalizeLimit(limit);
        LambdaQueryWrapper<KnowledgeArticle> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(KnowledgeArticle::getTitle, keyword).or()
                    .like(KnowledgeArticle::getContent, keyword);
        }
        if (StrUtil.isNotBlank(tag)) {
            wrapper.like(KnowledgeArticle::getTags, tag);
        }
        wrapper.orderByDesc(KnowledgeArticle::getUpdatedAt).last("limit " + rows);
        return knowledgeArticleMapper.selectList(wrapper);
    }

    @Override
    public List<KnowledgeArticle> searchTopArticles(String keyword, int limit) {
        return search(keyword, null, limit);
    }

    @Override
    public void delete(Long id) {
        knowledgeArticleMapper.deleteById(id);
        try {
            ragService.ifPresent(r -> r.deleteArticle(id));
        } catch (Exception ignored) {
        }
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) return 20;
        return Math.min(limit, MAX_LIMIT);
    }
}
