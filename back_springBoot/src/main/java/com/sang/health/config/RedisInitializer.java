package com.sang.health.config;

import com.sang.health.entity.BoardCount;
import com.sang.health.entity.BoardLike;
import com.sang.health.entity.BoardLikeTotal;
import com.sang.health.redis.RedisUtil;
import com.sang.health.repository.BoardCountRepository;
import com.sang.health.repository.BoardLikeRepository;
import com.sang.health.repository.BoardLikeTotalRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

// Redis 시작 시 BoardId 등록
@Configuration
public class RedisInitializer {

    private final BoardCountRepository boardCountRepository;
    private final RedisUtil redisUtil;
    private final BoardLikeTotalRepository boardLikeTotalRepository;
    private final BoardLikeRepository boardLikeRepository;

    public RedisInitializer(BoardCountRepository boardCountRepository, RedisUtil redisUtil, BoardLikeTotalRepository boardLikeTotalRepository, BoardLikeRepository boardLikeRepository) {
        this.boardCountRepository = boardCountRepository;
        this.redisUtil = redisUtil;
        this.boardLikeTotalRepository = boardLikeTotalRepository;
        this.boardLikeRepository = boardLikeRepository;
    }

    @Bean
    public CommandLineRunner initializeRedisData() {
        return args -> {
            // 조회수 데이터 로드
        	List<BoardCount> allBoardCounts = boardCountRepository.findAll();
        	
        	redisUtil.loadAllBoardViewsWithCount(allBoardCounts);
            
            System.out.println("Redis에 " + allBoardCounts.size() + "개의 게시글 조회수 정보 로드 완료");
            
            // 총 추천수 데이터 로드
            List<BoardLikeTotal> allBoardLikeTotals = boardLikeTotalRepository.findAll();
              
            redisUtil.loadAllBoardLikeTotal(allBoardLikeTotals);

            System.out.println("Redis에 " + allBoardLikeTotals.size() + "개의 게시글 추천 수 정보 로드 완료");
            
            // 개별 사용자 추천 정보 로드
            List<BoardLike> allBoardLikes = boardLikeRepository.findAll();
            
            redisUtil.loadAllBoardLikes(allBoardLikes);
            
            System.out.println("Redis에 " + allBoardLikes.size() + "개의 사용자 추천 정보 로드 완료");
        };
    }
}