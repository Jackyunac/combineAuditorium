package com.combine.auditorium.controller;

import com.combine.auditorium.client.AiClient;
import com.combine.auditorium.common.Result;
import com.combine.auditorium.common.RoleConstants;
import com.combine.auditorium.entity.AIConfig;
import com.combine.auditorium.entity.User;
import com.combine.auditorium.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AiClient aiClient;
    private final UserService userService;

    @PostMapping(value = "/chat", produces = "application/json;charset=UTF-8")
    public Result<String> chat(@RequestBody Map<String, String> body) {
        return aiClient.chat(body);
    }

    @GetMapping("/config")
    public Result<AIConfig> getConfig() {
        return aiClient.getConfig();
    }

    @PostMapping("/config")
    public Result<AIConfig> updateConfig(@RequestBody AIConfig config, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        User user = userService.getById(userId);
        if (user == null || !RoleConstants.SYSTEM.equals(user.getRole())) {
            return Result.error(403, "Permission denied");
        }
        return aiClient.updateConfig(config);
    }
}
