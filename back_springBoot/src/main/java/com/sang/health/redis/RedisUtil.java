package com.sang.health.redis;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.sang.health.entity.BoardCount;

@Component
public class RedisUtil {

	// Redis와의 통신 추상화 클래스
	private final RedisTemplate<String, String> redisTemplate;
    
    public RedisUtil(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    // Refresh Token
    // 생성
    public void saveRefreshToken(String username, String refreshToken, long expiredTime) {
    	// 브라우저 마다 관리: deviceId
    	String key = "RT:" + username;
    	redisTemplate.opsForValue().set(key, refreshToken, expiredTime, TimeUnit.MILLISECONDS);
    }
    
    // 값 조회
    public String getData(String key) {
    	
        return redisTemplate.opsForValue().get(key);
    }
    
    // 삭제
    public void deleteData(String key) {
        redisTemplate.delete(key);
    }
    
    // 존재하는지
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    // 조회수
    // 새 게시글이 작성되는 경우 (개별 저장)
    public void saveBoardView(Long boardId) {
        String key = "board:view:" + boardId;
        // 초기 조회수 0
        redisTemplate.opsForValue().set(key, "0");
    }
    
    // 조회수 증가
    public void incrementBoardView(Long boardId) {
        String key = "board:view:" + boardId;
        redisTemplate.opsForValue().increment(key, 1);
    }
    
    // 조회수 조회
    public int getBoardView(Long boardId) {
        String key = "board:view:" + boardId;
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value) : 0; // null인 경우 0, 연결 문제 시 NullPointerException 방지
    }
    
    // 모든 게시글 조회수 저장
    public void loadAllBoardViewsWithCount(List<BoardCount> boards) {
        for (BoardCount board : boards) {
        	String key = "board:view:" + board.getBoardId();
        	// 이미 등록된 경우 스킵
        	redisTemplate.opsForValue().set(key, String.valueOf(board.getCount()));
        }
    }
    
    // 삭제
    public void deleteBoardView(Long boardId) {
        String key = "board:view:" + boardId;
        redisTemplate.delete(key);
    }
    
    // (board:view:*) key 찾기
    public Set<String> getKeysWithPattern(String pattern) {
        return redisTemplate.keys(pattern);
    }
    
    // 무분별한 조회수 방지
    public boolean canIncrementView(String username, Long boardId) {
        String key = "view:" + username + ":" + boardId;
        
        // 키가 존재하지 않으면 조회수 증가 가능
        if (!redisTemplate.hasKey(key)) {
            // 24시간 동안 유효한 키 설정 (TTL 설정)
            redisTemplate.opsForValue().set(key, "1", 24, TimeUnit.HOURS);
            return true;
        }
        
        // 키가 이미 존재하면 조회수 증가 불가
        return false;
    }
}