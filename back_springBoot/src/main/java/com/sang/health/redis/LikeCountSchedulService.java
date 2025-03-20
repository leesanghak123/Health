package com.sang.health.redis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j // log
@Service
public class LikeCountSchedulService {
    private final RedisUtil redisUtil;
    private final JdbcTemplate jdbcTemplate;
    
    public LikeCountSchedulService(RedisUtil redisUtil, JdbcTemplate jdbcTemplate) {
        this.redisUtil = redisUtil;
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void likeCountToDB() {
        // Redis에서 추천수 관련 키 패턴 조회
        Set<String> keys = redisUtil.getKeysWithPattern("board:like:total:*");
        
        if (keys.isEmpty()) {
            return;
        }
        
        // 배치 처리를 위한 리스트
        List<Object[]> batchArgs = new ArrayList<>();
        
        for (String key : keys) {
            try {
                // boardId 파싱
                String boardIdStr = key.substring("board:like:total:".length());
                Long boardId = Long.parseLong(boardIdStr);
                
                // Redis에서 추천수 가져오기
                int likeCount = redisUtil.getBoardLikeTotal(boardId);
                
                batchArgs.add(new Object[] {likeCount, boardId});
                
            } catch (Exception e) {
                log.error("추천수 동기화 중 오류 발생: " + key, e);
            }
        }
        
        // 배치로 한 번에 저장
        if (!batchArgs.isEmpty()) {
            int[] updateCounts = this.jdbcTemplate.batchUpdate(
                "UPDATE boardLikeTotal SET likeTotal = ? WHERE boardId = ?",
                batchArgs
            );
            
            log.info("추천수 업데이트된 게시판 수: " + Arrays.stream(updateCounts).sum());
        }
    }
    
    // 사용자 추천 정보 동기화
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void syncUserLikesToDB() {
        // 새로운 사용자 추천 정보만 DB에 추가
        Set<String> keys = redisUtil.getKeysWithPattern("board:like:*");
        
        if (keys.isEmpty()) {
            return;
        }
        
        List<Object[]> batchArgs = new ArrayList<>();
        
        for (String key : keys) {
            try {
                // board:like:total인 경우
                String[] parts = key.split(":");
                if (parts[2].equals("total")) continue;
                
                Long boardId = Long.parseLong(parts[2]);
                String username = parts[3];
                
                batchArgs.add(new Object[] {boardId, username});
                
            } catch (Exception e) {
                log.error("사용자 추천 정보 동기화 중 오류 발생: " + key, e);
            }
        }
        
        if (!batchArgs.isEmpty()) {
            int[] insertCounts = this.jdbcTemplate.batchUpdate(
                "INSERT IGNORE INTO boardLike (boardId, username) VALUES (?, ?)",
                batchArgs
            );
            
            log.info("추가된 사용자 추천 수: " + Arrays.stream(insertCounts).sum());
        }
    }
}