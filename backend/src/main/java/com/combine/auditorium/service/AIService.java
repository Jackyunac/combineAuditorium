package com.combine.auditorium.service;

import com.combine.auditorium.config.AIPromptConfig;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final AIPromptConfig promptConfig;

    @Value("${google.ai.api-key}")
    private String apiKey;

    @Value("${google.ai.model}")
    private String modelName;

    @Value("${google.ai.base-url}")
    private String baseUrl;

    private Client client;

    @PostConstruct
    public void init() {
        try {
            // 初始化 Google GenAI Client
            if (apiKey != null && !apiKey.equals("any")) {
                Client.Builder builder = Client.builder()
                        .apiKey(apiKey);
                
                // 如果配置了自定义 Base URL（反向代理），通过 HttpOptions 设置
                if (baseUrl != null && !baseUrl.isEmpty() && !baseUrl.contains("generativelanguage.googleapis.com")) {
                    log.info("Using custom AI Base URL: {}", baseUrl);
                    HttpOptions httpOptions = HttpOptions.builder()
                            .baseUrl(baseUrl)
                            .build();
                    builder.httpOptions(httpOptions);
                }
                
                client = builder.build();
                log.info("Google GenAI Client initialized for model: {}", modelName);
                log.info("Loaded System Instruction length: {}", promptConfig.getSystemInstruction().length());
            } else {
                log.warn("Google AI API Key not configured. AI features will be disabled.");
            }
        } catch (Exception e) {
            log.error("Failed to initialize Google GenAI Client", e);
        }
    }

    /**
     * 发送消息给 AI 并获取回复
     *
     * @param message 用户消息
     * @return AI 回复
     */
    public String chat(String message) {
        if (client == null) {
            return "AI 服务未配置或初始化失败。请检查 API Key。";
        }

        try {
            // 构建配置，包含 System Instruction
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .systemInstruction(
                        Content.builder()
                            .parts(Collections.singletonList(Part.builder().text(promptConfig.getSystemInstruction()).build()))
                            .build()
                    )
                    .build();

            // 发送请求
            GenerateContentResponse response = client.models.generateContent(modelName, message, config);
            String responseText = response.text();
            
            // 尝试修复乱码 (ISO-8859-1 -> UTF-8)
            if (responseText != null) {
                String fixedText = new String(responseText.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                log.info("Fixed Response (UTF-8): {}", fixedText.length() > 100 ? fixedText.substring(0, 100) + "..." : fixedText);
                return fixedText;
            }
            
            return responseText;
        } catch (Exception e) {
            log.error("Error during AI chat", e);
            return "AI 暂时无法回答: " + e.getMessage();
        }
    }
}
