package com.combine.auditorium.rag;

import dev.langchain4j.store.embedding.pgvector.MetadataStorageConfig;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;

import javax.sql.DataSource;

/**
 * 公共构造封装，便于在 Spring 中直接创建 PgVectorEmbeddingStore。
 */
public class PublicPgVectorStore extends PgVectorEmbeddingStore {
    public PublicPgVectorStore(DataSource dataSource,
                               String table,
                               Integer dimensions,
                               Boolean createTable,
                               Integer lists,
                               Boolean useIndex,
                               Boolean normalize,
                               MetadataStorageConfig metadataStorageConfig) {
        super(dataSource, table, dimensions, createTable, lists, useIndex, normalize, metadataStorageConfig);
    }
}
