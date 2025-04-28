package com.sang.health.util;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.sang.health.entity.board.BoardCount;
import com.sang.health.entity.board.BoardLike;
import com.sang.health.entity.board.BoardLikeTotal;

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
    
    // 무분별한 조회수 방지 삭제
    public void deletecanIncrementView(String username, Long boardId) {
        String key = "view:" + username + ":" + boardId;
        redisTemplate.delete(key);
    }
    
    
    
    // 추천수
    // 새 게시글이 작성되는 경우 초기화
    public void saveBoardLikeTotal(Long boardId) {
    	String key = "board:like:total:" + boardId;
    	redisTemplate.opsForValue().set(key, "0");
    }
    
    // 게시글 총 추천수 삭제
    public void deleteBoardLikeTotal(Long boardId) {
        String key = "board:like:total:" + boardId;
        redisTemplate.delete(key);
    }
    
    // 총 추천수 조회
    public int getBoardLikeTotal(Long boardId) {
    	String key = "board:like:total:" + boardId;
    	String value = redisTemplate.opsForValue().get(key);
    	return value != null ? Integer.parseInt(value) : 0;
    }
    
    // 게시글 총 추천수 증가
    public void incrementBoardLike(Long boardId) {
        String key = "board:like:total:" + boardId;
        redisTemplate.opsForValue().increment(key, 1);
    }
    
    // 게시글 총 추천수 감소
    public void decrementBoardLike(Long boardId) {
        String key = "board:like:total:" + boardId;
        Long currentValue = redisTemplate.opsForValue().increment(key, -1);
        
        // 음수 방지
        if (currentValue != null && currentValue < 0) {
            redisTemplate.opsForValue().set(key, "0");
        }
    }
    
    // 모든 게시글 추천수 저장
    public void loadAllBoardLikeTotal(List<BoardLikeTotal> boards) {
        for (BoardLikeTotal board : boards) {
        	String key = "board:like:total:" + board.getBoardId();
        	// 이미 등록된 경우 스킵
        	redisTemplate.opsForValue().set(key, String.valueOf(board.getLikeTotal()));
        }
    }
    
    // 사용자 추천 여부 확인
    public boolean getBoardLike(Long boardId, String username) {
        String key = "board:like:check:" + boardId + ":" + username;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    // 사용자 추천 생성
    public void createBoardLike(Long boardId, String username) {
        String key = "board:like:check:" + boardId + ":" + username;
        redisTemplate.opsForValue().set(key, "1");
    }
    
    // 사용자 좋아요 삭제
    public void deleteBoardLike(Long boardId, String username) {
        String key = "board:like:check:" + boardId + ":" + username;
        redisTemplate.delete(key);
    }
    
    // 특정 게시글의 모든 추천 삭제
    public void deleteBoardLike(Long boardId) {
        String pattern = "board:like:check" + boardId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
    
    // 사용자 추천 정보 로드
    public void loadAllBoardLikes(List<BoardLike> allBoardLikes) {
        for (BoardLike like : allBoardLikes) {
            createBoardLike(like.getBoardId(), like.getUsername());
        }
    }
}