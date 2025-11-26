package com.combine.auditorium.controller;

import com.combine.auditorium.client.AiClient;
import com.combine.auditorium.common.Result;
import com.combine.auditorium.entity.KnowledgeArticle;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final AiClient aiClient;

    @PostMapping
    public Result<KnowledgeArticle> create(@RequestBody KnowledgeArticle article, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        return aiClient.createKnowledge(article);
    }

    @GetMapping
    public Result<List<KnowledgeArticle>> search(@RequestParam(value = "q", required = false) String keyword,
                                                 @RequestParam(value = "tag", required = false) String tag,
                                                 @RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        return aiClient.searchKnowledge(keyword, tag, limit);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        return aiClient.deleteKnowledge(id);
    }
}
