package com.combine.auditorium.controller;

import com.combine.auditorium.common.Result;
import com.combine.auditorium.entity.KnowledgeArticle;
import com.combine.auditorium.service.KnowledgeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @PostMapping
    public Result<KnowledgeArticle> create(@RequestBody KnowledgeArticle article, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        return Result.success(knowledgeService.create(article, userId));
    }

    @GetMapping
    public Result<List<KnowledgeArticle>> search(@RequestParam(value = "q", required = false) String keyword,
                                                 @RequestParam(value = "tag", required = false) String tag,
                                                 @RequestParam(value = "limit", defaultValue = "20") Integer limit) {
        return Result.success(knowledgeService.search(keyword, tag, limit));
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        knowledgeService.delete(id);
        return Result.success("Deleted");
    }
}
