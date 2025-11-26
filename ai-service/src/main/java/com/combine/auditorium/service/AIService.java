package com.combine.auditorium.service;

import com.combine.auditorium.config.AIPromptConfig;
import com.combine.auditorium.entity.AIConfig;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;

import com.combine.auditorium.service.RagService;

@Slf4j
@Service
public class AIService {

    private final AIPromptConfig promptConfig;
    private final AIConfigService aiConfigService;
    private final Optional<RagService> ragService;

    @Value("${google.ai.api-key}")
    private String apiKey;

    @Value("${google.ai.model}")
    private String modelName;

    @Value("${google.ai.base-url}")
    private String baseUrl;

    private Client client;

    public AIService(AIPromptConfig promptConfig, AIConfigService aiConfigService, Optional<RagService> ragService) {
        this.promptConfig = promptConfig;
        this.aiConfigService = aiConfigService;
        this.ragService = ragService;
    }

    @PostConstruct
    public void init() {
        try {
            if (apiKey != null && !apiKey.equals("any")) {
                Client.Builder builder = Client.builder()
                        .apiKey(apiKey);

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

    public String chat(String message) {
        if (client == null) {
            return "AI service not configured or initialization failed. Check API Key.";
        }

        try {
            AIConfig cfg = aiConfigService.getConfig();
            GenerateContentConfig.Builder cfgBuilder = GenerateContentConfig.builder()
                    .systemInstruction(
                        Content.builder()
                            .parts(Collections.singletonList(Part.builder().text(promptConfig.getSystemInstruction()).build()))
                            .build()
                    );
            if (cfg.getTemperature() != null) {
                cfgBuilder.temperature(cfg.getTemperature().floatValue());
            }
            if (cfg.getTopK() != null) {
                cfgBuilder.topK(cfg.getTopK().floatValue());
            }

            String prompt = ragService.map(r -> r.buildPromptWithKnowledge(message, cfg)).orElse(message);
            GenerateContentResponse response = client.models.generateContent(modelName, prompt, cfgBuilder.build());
            String responseText = response.text();

            if (responseText != null) {
                String fixedText = new String(responseText.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                log.info("Fixed Response (UTF-8): {}", fixedText.length() > 100 ? fixedText.substring(0, 100) + "..." : fixedText);
                return fixedText;
            }

            return responseText;
        } catch (Exception e) {
            log.error("Error during AI chat", e);
            return "AI cannot respond now: " + e.getMessage();
        }
    }
}
