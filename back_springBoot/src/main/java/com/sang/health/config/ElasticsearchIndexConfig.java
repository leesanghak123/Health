package com.sang.health.config;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;

import com.sang.health.entity.BoardES;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ElasticsearchIndexConfig {

    private final ElasticsearchOperations elasticsearchOperations;

    public ElasticsearchIndexConfig(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @PostConstruct
    public void createIndex() {
        try {
            IndexOperations indexOperations = elasticsearchOperations.indexOps(BoardES.class);

            // 인덱스가 존재하지 않는 경우에만 생성
            if (!indexOperations.exists()) {
                // Mappings 생성
                Map<String, Object> mappings = Map.of(
                    "properties", Map.of(
                        "id", Map.of(
                            "type", "long",
                            "index", false
                        ),
                        "title", Map.of(
                            "type", "text",
                            "analyzer", "my_custom_analyzer"
                        ),
                        "content", Map.of(
                            "type", "text",
                            "analyzer", "my_custom_analyzer"
                        ),
                        "created", Map.of(
                            "type", "date",
                            "index", false
                        )
                    )
                );

                // 분석기 및 필터 설정
                Map<String, Object> analysis = Map.of(
                    "analyzer", Map.of(
                        "my_custom_analyzer", Map.of(
                            "type", "custom",
                            "char_filter", List.of("my_html_strip"),
                            "tokenizer", "my_nori_tokenizer",
                            "filter", List.of("my_pos_filter", "lowercase_filter", "synonym_filter")
                        )
                    ),
                    "char_filter", Map.of(
                        "my_html_strip", Map.of(
                            "type", "html_strip"
                        )
                    ),
                    "tokenizer", Map.of(
                        "my_nori_tokenizer", Map.of(
                            "type", "nori_tokenizer",
                            "decompound_mode", "mixed",
                            "discard_punctuation", "true",
                            "user_dictionary", "dict/userdict_ko.txt"
                        )
                    ),
                    "filter", Map.of(
                        "my_pos_filter", Map.of(
                            "type", "nori_part_of_speech",
                            "stoptags", List.of("J")
                        ),
                        "lowercase_filter", Map.of(
                            "type", "lowercase"
                        ),
                        "synonym_filter", Map.of(
                            "type", "synonym",
                            "synonyms_path", "dict/synonym-set.txt"
                        )
                    )
                );

                // 기본 설정
                Map<String, Object> settings = Map.of(
                    "number_of_shards", 1,
                    "number_of_replicas", 0,
                    "analysis", analysis
                );

                // 인덱스 생성
                indexOperations.create(settings);
                indexOperations.putMapping(Document.from(mappings));

                log.info("BoardES 인덱스 생성 완료");
            } else {
                log.info("BoardES 인덱스 이미 존재");
            }
        } catch (Exception e) {
            log.error("인덱스 생성 중 오류 발생", e);
            throw new RuntimeException("Elasticsearch 인덱스 생성 실패", e);
        }
    }
}