package com.sang.health.batch;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import com.sang.health.entity.board.BoardCount;

@Configuration
public class ViewBatch {
	private final JobRepository jobRepository;
	private final PlatformTransactionManager platformTransactionManager;
	private final RedisTemplate<String, String> redisTemplate;
	private final DataSource dataSource;
	
	public ViewBatch(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, RedisTemplate<String, String> redisTemplate, @Qualifier("dataDBSource")DataSource dataSource) {
		this.jobRepository = jobRepository;
		this.platformTransactionManager = platformTransactionManager;
		this.redisTemplate = redisTemplate;
		this.dataSource = dataSource;
		
	}
	
	@Bean
	Job ViewJob() {
		
		System.out.println("View Job");
		
		return new JobBuilder("ViewJob", jobRepository)
				.start(viewStep())
				.build();
	}
	
	@Bean
	Step viewStep() {
		
		System.out.println("view Step");
		
	    return new StepBuilder("ViewStep", jobRepository)
	        .<Map.Entry<String, String>, BoardCount>chunk(10, platformTransactionManager)
	        .reader(viewReader())
	        .processor(viewProcessor())
	        .writer(viewWriter())
	        .build();
	}

	
	
	// read
	@Bean
	ItemStreamReader<Map.Entry<String, String>> viewReader() {
	    return new RedisKeyValueItemReader(redisTemplate, "board:view:*", 10, 10); // 100개씩 요청
	}
	
	// process
	@Bean
	ItemProcessor<Map.Entry<String, String>, BoardCount> viewProcessor() {
	    return entry -> {
	        String key = entry.getKey();
	        String value = entry.getValue();

	        Long boardId = Long.parseLong(key.replace("board:view:", ""));
	        int count = value != null ? Integer.parseInt(value) : 0;

	        BoardCount boardCount = new BoardCount();
	        boardCount.setBoardId(boardId);
	        boardCount.setCount(count);

	        return boardCount;
	    };
	}
	
	// write
	@Bean
    JdbcBatchItemWriter<BoardCount> viewWriter() {
		
		String sql = "UPDATE BoardCount SET count = :count WHERE boardId = :boardId"; 
		
        return new JdbcBatchItemWriterBuilder<BoardCount>()
                .dataSource(dataSource)
                .sql(sql)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }
}
