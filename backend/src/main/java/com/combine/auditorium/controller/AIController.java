package com.combine.auditorium.controller;

import com.combine.auditorium.common.Result;
import com.combine.auditorium.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    @PostMapping(value = "/chat", produces = "application/json;charset=UTF-8")
    public Result<String> chat(@RequestBody Map<String, String> body) {
        try {
            String message = body.get("message");
            log.info("Received chat request");
            String response = aiService.chat(message);
            return Result.success(response);
        } catch (Exception e) {
            log.error("Error in chat endpoint", e);
            return Result.error("AI 服务异常: " + e.getMessage());
        }
    }
}

