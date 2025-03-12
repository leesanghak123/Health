package com.sang.health.jwt;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisUtil {

	// Redis와의 통신 추상화 클래스
	private final RedisTemplate<String, String> redisTemplate;
    
    public RedisUtil(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    // 생성
    public void saveRefreshToken(String username, String refreshToken, long expiredTime) {
    	// 브라우저 마다 관리: deviceId
    	String key = "RT:" + username;
    	redisTemplate.opsForValue().set("RT:" + username, refreshToken, expiredTime, TimeUnit.MILLISECONDS);
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
}