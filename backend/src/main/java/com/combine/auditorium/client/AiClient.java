package com.combine.auditorium.client;

import com.combine.auditorium.common.Result;
import com.combine.auditorium.entity.AIConfig;
import com.combine.auditorium.entity.KnowledgeArticle;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "ai-service", url = "${ai.service.url:http://localhost:8082}", path = "/api")
public interface AiClient {

    @PostMapping("/ai/chat")
    Result<String> chat(@RequestBody Map<String, String> body);

    @GetMapping("/ai/config")
    Result<AIConfig> getConfig();

    @PostMapping("/ai/config")
    Result<AIConfig> updateConfig(@RequestBody AIConfig config);

    @PostMapping("/knowledge")
    Result<KnowledgeArticle> createKnowledge(@RequestBody KnowledgeArticle article);

    @GetMapping("/knowledge")
    Result<List<KnowledgeArticle>> searchKnowledge(@RequestParam(value = "q", required = false) String keyword,
                                                   @RequestParam(value = "tag", required = false) String tag,
                                                   @RequestParam(value = "limit", defaultValue = "20") Integer limit);

    @DeleteMapping("/knowledge/{id}")
    Result<String> deleteKnowledge(@PathVariable("id") Long id);
}
