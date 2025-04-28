package com.sang.health.batch;

import java.util.Map;

import javax.sql.DataSource;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import com.sang.health.entity.board.Board;
import com.sang.health.entity.board.BoardES;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ESBatch {
	private final JobRepository jobRepository;
	private final PlatformTransactionManager platformTransactionManager;
	private final ElasticsearchOperations elasticsearchOperations;
	private final DataSource dataSource;
	
	private static final String BOARD_INDEX = "board-index";
	
	public ESBatch(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, ElasticsearchOperations elasticsearchOperations, @Qualifier("dataDBSource")DataSource dataSource) {
		this.jobRepository = jobRepository;
		this.platformTransactionManager = platformTransactionManager;
		this.elasticsearchOperations = elasticsearchOperations;
		this.dataSource = dataSource;
	}
	
	@Bean
	Job ESJob() {
		
		System.out.println("ES Job");
		
		return new JobBuilder("ESJob", jobRepository)
				.start(deleteIndexStep()) // 기존 인덱스 제거
                .next(createIndexStep())  // 인덱스 생성
                .next(ESStep())           // 마이그레이션 
				.build();
	}
	
	@Bean
    Step deleteIndexStep() {
        return new StepBuilder("deleteIndexStep", jobRepository)
                .tasklet((contribution, chunkContext) -> { // tasklet: 하나의 작업 단위
                    log.info("Executing delete index step");
                    IndexOperations indexOperations = elasticsearchOperations.indexOps(IndexCoordinates.of(BOARD_INDEX));
                    
                    if (indexOperations.exists()) {
                        indexOperations.delete();
                        log.info("Index {} deleted successfully", BOARD_INDEX);
                    } else {
                        log.info("Index {} does not exist, skipping deletion", BOARD_INDEX);
                    }
                    return RepeatStatus.FINISHED;
                }, platformTransactionManager)
                .build();
    }
	
	@Bean
    Step createIndexStep() {
        return new StepBuilder("createIndexStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("Executing create index step");
                    IndexOperations indexOperations = elasticsearchOperations.indexOps(IndexCoordinates.of(BOARD_INDEX));
                    
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
                            "createDate", Map.of(
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
                    
                    log.info("Index {} created successfully with mappings", BOARD_INDEX);
                    return RepeatStatus.FINISHED;
                }, platformTransactionManager)
                .build();
    }
	
	@Bean
	Step ESStep() {
		
		System.out.println("ES Step");
		
		return new StepBuilder("ESStep", jobRepository)
				.<Board, BoardES> chunk(10, platformTransactionManager)
				.reader(boardReader())
	            .processor(boardToESProcessor())
	            .writer(boardEsWriter())
	            .build();
	}
	
	
	// read
	@Bean
	JdbcPagingItemReader<Board> boardReader() {
		return new JdbcPagingItemReaderBuilder<Board>()
				.name("boardReader")
				.dataSource(dataSource)
				.selectClause("SELECT id, title, content, createDate")
	            .fromClause("FROM Board")
	            .sortKeys(Map.of("id", Order.ASCENDING))
	            .rowMapper(new BeanPropertyRowMapper<>(Board.class))
	            .pageSize(10)
	            .build();
	}
	
	// process
	@Bean
	ItemProcessor<Board, BoardES> boardToESProcessor() {
		return board -> {
			BoardES es = new BoardES();
			es.setId(board.getId());
            es.setTitle(board.getTitle());
            es.setContent(board.getContent());
            es.setCreateDate(board.getCreateDate());
            return es;
		};
	}

	// write
	@Bean
    ItemWriter<BoardES> boardEsWriter() {
        return new ElasticsearchBulkItemWriter<>(
        		elasticsearchOperations,
                BOARD_INDEX
        		);
    }
}
