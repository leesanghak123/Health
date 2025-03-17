package com.sang.health.config;

import com.sang.health.entity.BoardCount;
import com.sang.health.redis.RedisUtil;
import com.sang.health.repository.BoardCountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

// Redis 시작 시 BoardId 등록
@Configuration
public class RedisInitializer {

    private final BoardCountRepository boardCountRepository;
    private final RedisUtil redisUtil;

    public RedisInitializer(BoardCountRepository boardCountRepository, RedisUtil redisUtil) {
        this.boardCountRepository = boardCountRepository;
        this.redisUtil = redisUtil;
    }

    @Bean
    public CommandLineRunner initializeRedisData() {
        return args -> {
            // DB에서 모든 게시글 ID 가져오기
        	List<BoardCount> allBoardCounts = boardCountRepository.findAll();
        	
        	redisUtil.loadAllBoardViewsWithCount(allBoardCounts);
            
            System.out.println("Redis에 " + allBoardCounts.size() + "개의 게시글 조회수 정보 로드 완료");
        };
    }
}