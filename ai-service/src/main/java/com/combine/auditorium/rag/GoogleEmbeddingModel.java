package com.combine.auditorium.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Google Embedding 调用封装，适配 LangChain4j EmbeddingModel 接口。
 */
@Slf4j
public class GoogleEmbeddingModel implements EmbeddingModel {

    private final String apiKey;
    private final String model;
    private final String baseUrl;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GoogleEmbeddingModel(String apiKey, String model, String baseUrl) {
        Assert.hasText(apiKey, "Google AI apiKey is required");
        Assert.hasText(model, "Google embedding model name is required");
        this.apiKey = apiKey;
        this.model = model;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Response<List<Embedding>> embedAll(List<TextSegment> segments) {
        List<Embedding> embeddings = segments.stream()
                .map(seg -> embedSingle(seg.text()))
                .collect(Collectors.toList());
        return Response.from(embeddings, new TokenUsage(null, null, null));
    }

    @Override
    public Response<Embedding> embed(TextSegment textSegment) {
        return Response.from(embedSingle(textSegment.text()), new TokenUsage(null, null, null));
    }

    private Embedding embedSingle(String text) {
        try {
            String endpoint = "%s/v1beta/models/%s:embedContent?key=%s".formatted(baseUrl, model, apiKey);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String payload = """
                {
                  "content": { "parts": [ { "text": %s } ] }
                }
                """.formatted(objectMapper.writeValueAsString(text));
            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
            String body = restTemplate.postForObject(URI.create(endpoint), entity, String.class);
            JsonNode node = objectMapper.readTree(body);
            JsonNode values = node.path("embedding").path("values");
            if (values.isMissingNode() || !values.isArray()) {
                throw new IllegalStateException("Embedding response missing values");
            }
            float[] vector = new float[values.size()];
            for (int i = 0; i < values.size(); i++) {
                vector[i] = (float) values.get(i).asDouble();
            }
            return Embedding.from(vector);
        } catch (Exception e) {
            log.error("Failed to embed text", e);
            throw new RuntimeException("Embed failed: " + e.getMessage(), e);
        }
    }
}
