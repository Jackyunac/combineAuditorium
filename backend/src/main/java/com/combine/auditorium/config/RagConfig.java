package com.combine.auditorium.config;

import com.combine.auditorium.rag.GoogleEmbeddingModel;
import com.zaxxer.hikari.HikariDataSource;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.DefaultMetadataStorageConfig;
import com.combine.auditorium.rag.PublicPgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class RagConfig {

    @Value("${vector.datasource.url:}")
    private String vectorUrl;
    @Value("${vector.datasource.username:}")
    private String vectorUser;
    @Value("${vector.datasource.password:}")
    private String vectorPassword;
    @Value("${vector.datasource.table:kb_vectors}")
    private String vectorTable;
    @Value("${google.ai.embedding-model:embedding-001}")
    private String embeddingModelName;
    @Value("${google.ai.api-key:}")
    private String apiKey;
    @Value("${google.ai.base-url:https://generativelanguage.googleapis.com}")
    private String baseUrl;

    @Bean
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${vector.datasource.url:}')")
    public DataSource vectorDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(vectorUrl);
        ds.setUsername(vectorUser);
        ds.setPassword(vectorPassword);
        ds.setMaximumPoolSize(5);
        return ds;
    }

    @Bean
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${vector.datasource.url:}')")
    public EmbeddingStore<TextSegment> embeddingStore(DataSource vectorDataSource) {
        // 默认使用 768 维度（Google Embedding 模型通常是 768），如有差异请在配置中调整表结构
        return new PublicPgVectorStore(
            vectorDataSource,
            vectorTable,
            Integer.valueOf(768),
            Boolean.TRUE,   // create table if not exists
            Integer.valueOf(100),    // ivfflat lists
            Boolean.TRUE,   // use index
            Boolean.TRUE,   // normalize embeddings
            DefaultMetadataStorageConfig.defaultConfig()
        );
    }

    @Bean
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${vector.datasource.url:}')")
    public GoogleEmbeddingModel googleEmbeddingModel() {
        return new GoogleEmbeddingModel(apiKey, embeddingModelName, baseUrl);
    }

}
