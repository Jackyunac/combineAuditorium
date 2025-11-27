package com.combine.auditorium.controller;

import com.combine.auditorium.common.Result;
import com.combine.auditorium.entity.KnowledgeArticle;
import com.combine.auditorium.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @PostMapping
    public Result<KnowledgeArticle> create(@RequestBody KnowledgeArticle article) {
        return Result.success(knowledgeService.create(article));
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
