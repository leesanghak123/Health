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

import com.sang.health.entity.board.BoardLike;

@Configuration
public class LikeCheckBatch {
	private final JobRepository jobRepository;
	private final PlatformTransactionManager platformTransactionManager;
	private final RedisTemplate<String, String> redisTemplate;
	private final DataSource dataSource;
	
	public LikeCheckBatch(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, RedisTemplate<String, String> redisTemplate, @Qualifier("dataDBSource")DataSource dataSource) {
		this.jobRepository = jobRepository;
		this.platformTransactionManager = platformTransactionManager;
		this.redisTemplate = redisTemplate;
		this.dataSource = dataSource;
	}
	
	@Bean
	Job LikeCheckJob() {
		
		System.out.println("LikeCheck Job");
		
		return new JobBuilder("LikeCheckJob", jobRepository)
				.start(LikeCheckStep())
				.build();
	}
	
	// 사용자별 추천 유무확인
	@Bean
	Step LikeCheckStep() {
		
		System.out.println("LikeCheck Step");
		
		return new StepBuilder("LikeCheckStep", jobRepository)
				.<Map.Entry<String, String>, BoardLike>chunk(10, platformTransactionManager)
				.reader(likeCheckReader())
	            .processor(likeCheckProcessor())
	            .writer(likeCheckWriter())
	            .build();
	}
	
	// read
	@Bean
	ItemStreamReader<Map.Entry<String, String>> likeCheckReader() {
	    return new RedisKeyValueItemReader(redisTemplate, "board:like:check:*", 10, 10);
	}
	
	// process
	@Bean
	ItemProcessor<Map.Entry<String, String>, BoardLike> likeCheckProcessor() {
	    return entry -> {
	        String key = entry.getKey();
	        String[] parts = key.split(":");

	        if (parts.length != 5) return null;

	        Long boardId = Long.parseLong(parts[3]);
	        String username = parts[4];

	        BoardLike boardLike = new BoardLike();
	        boardLike.setBoardId(boardId);
	        boardLike.setUsername(username);
	      
	        return boardLike;
	    };
	}


	
	// write
	@Bean
	JdbcBatchItemWriter<BoardLike> likeCheckWriter() {
		
		String sql = "INSERT INTO BoardLike (boardId, username) VALUES (:boardId, :username)";
		
	    return new JdbcBatchItemWriterBuilder<BoardLike>()
	            .dataSource(dataSource)
	            .sql(sql)
	            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
	            .build();
	}
}