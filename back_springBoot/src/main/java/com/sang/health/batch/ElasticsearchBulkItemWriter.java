package com.sang.health.batch;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ElasticsearchBulkItemWriter<T> implements ItemWriter<T> {

    private final ElasticsearchOperations elasticsearchOperations;
    private final String indexName;

    public ElasticsearchBulkItemWriter(ElasticsearchOperations elasticsearchOperations, String indexName) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.indexName = indexName;
    }

    @Override
    public void write(Chunk<? extends T> chunk) throws Exception {
        if (chunk.isEmpty()) {
            return;
        }

        long startTime = System.currentTimeMillis();
        List<IndexQuery> queries = new ArrayList<>(chunk.size());
        
        for (T item : chunk.getItems()) {
            IndexQuery indexQuery = new IndexQueryBuilder()
                .withObject(item)
                .build();
            queries.add(indexQuery);
        }

        // 일괄 작업 실행
        elasticsearchOperations.bulkIndex(
            queries,
            IndexCoordinates.of(indexName)
        );
        
        long endTime = System.currentTimeMillis();
        log.info("일괄 인덱싱 완료: {} 문서, 소요시간: {}ms", chunk.size(), (endTime - startTime));
    }
}