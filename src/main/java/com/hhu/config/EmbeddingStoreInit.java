package com.hhu.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class EmbeddingStoreInit {


    final PgConfig pgConfig;


    @Bean
    public EmbeddingStore<TextSegment> initEmbeddingStore() {

        return PgVectorEmbeddingStore.builder()
            .table(pgConfig.getTable())
            .dropTableFirst(false)
            .createTable(true)
            .host(pgConfig.getHost())
            .port(pgConfig.getPort())
            .user(pgConfig.getUser())
            .password(pgConfig.getPassword())
            .dimension(1024)
            .database(pgConfig.getDatabase())
            .build();

    }
}
