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
import com.sang.health.entity.board.BoardLikeTotal;

@Configuration
public class LikeCountBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final RedisTemplate<String, String> redisTemplate;
    private final DataSource dataSource;

    public LikeCountBatch(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, RedisTemplate<String, String> redisTemplate, @Qualifier("dataDBSource") DataSource dataSource) {
        this.jobRepository = jobRepository;
        this.platformTransactionManager = platformTransactionManager;
        this.redisTemplate = redisTemplate;
        this.dataSource = dataSource;
    }

    @Bean
    Job LikeCountJob() {
        System.out.println("LikeCount Job");
        return new JobBuilder("LikeCountJob", jobRepository)
                .start(LikeCountStep())
                .build();
    }

    @Bean
    Step LikeCountStep() {
        System.out.println("LikeCount Step");
        return new StepBuilder("LikeCountStep", jobRepository)
                .<Map.Entry<String, String>, BoardLikeTotal>chunk(10, platformTransactionManager)
                .reader(likeCountReader())
                .processor(likeCountProcessor())
                .writer(likeCountWriter())
                .build();
    }

    // Read
    @Bean
    ItemStreamReader<Map.Entry<String, String>> likeCountReader() {
        return new RedisKeyValueItemReader(redisTemplate, "board:like:total:*", 10, 10);
    }

    // Process
    @Bean
    ItemProcessor<Map.Entry<String, String>, BoardLikeTotal> likeCountProcessor() {
        return entry -> {
            String key = entry.getKey();
            String value = entry.getValue();

            String[] parts = key.split(":");

            Long boardId = Long.parseLong(parts[3]);
            int likeTotal = Integer.parseInt(value);

            BoardLikeTotal boardLikeTotal = new BoardLikeTotal();
            boardLikeTotal.setBoardId(boardId);
            boardLikeTotal.setLikeTotal(likeTotal);

            return boardLikeTotal;
        };
    }

    // write
    @Bean
    JdbcBatchItemWriter<BoardLikeTotal> likeCountWriter() {
    	
    	String sql = "UPDATE BoardLikeTotal SET likeTotal = :likeTotal WHERE boardId = :boardId";
    	
        return new JdbcBatchItemWriterBuilder<BoardLikeTotal>()
        		.dataSource(dataSource)
                .sql(sql)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }
}
