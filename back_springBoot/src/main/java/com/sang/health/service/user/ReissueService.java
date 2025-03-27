package com.sang.health.service.user;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sang.health.jwt.JWTUtil;
import com.sang.health.redis.RedisUtil;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.transaction.Transactional;

@Service
public class ReissueService {
    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;

    public ReissueService(JWTUtil jwtUtil, RedisUtil redisUtil) {
        this.jwtUtil = jwtUtil;
        this.redisUtil = redisUtil;
    }
    
    @Transactional
    public Map<String, Object> reissueAccessToken(String refreshToken, String deviceId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 토큰 만료 확인
            jwtUtil.isExpired(refreshToken);
            
            // 토큰 카테고리 확인
            String category = jwtUtil.getCategory(refreshToken);
            if (!category.equals("refresh")) {
                result.put("success", false);
                result.put("message", "invalid refresh token");
                return result;
            }
            
            // 사용자 정보 추출
            String username = jwtUtil.getUsername(refreshToken);
            
            // Redis에 저장된 토큰 확인
            String key = "RT:" + username + deviceId;
            String storedToken = redisUtil.getData(key);
            
            // Redis에 저장된 토큰이 없거나 요청된 토큰과 다른 경우
            if (storedToken == null || !storedToken.equals(refreshToken)) {
                result.put("success", false);
                result.put("message", "invalid refresh token");
                return result;
            }
            
            // 사용자 정보 추출
            String role = jwtUtil.getRole(refreshToken);
            

            // 새로운 Access 토큰 생성 (10분 유효) (현재 6초)
            String newAccessToken = jwtUtil.createJwt("access", username, role, 60000L);
            
            // 새로운 deviceId 생성
            String newDeviceId = UUID.randomUUID().toString();
            
            // Refresh Rotate를 하기 위한 Refresh 생성
            String newRefresh = jwtUtil.createJwt("refresh", username, role, 86400000L);
            
            // 기존의 Refresh 토큰 삭제
            redisUtil.deleteData(key);

            // 새 Refresh 토큰 저장
            redisUtil.saveRefreshToken(username+newDeviceId, newRefresh, 86400000L);

            // 성공 결과 반환
            result.put("success", true);
            result.put("accessToken", newAccessToken);
            result.put("refreshTokenCookie", jwtUtil.createRefreshTokenCookie(newRefresh));
            result.put("newDeviceIdCookie", jwtUtil.createDeviceIdCookie(newDeviceId));
            return result;

        } catch (ExpiredJwtException e) {
            result.put("success", false);
            result.put("message", "refresh token expired");
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "token processing error: " + e.getMessage());
            e.printStackTrace();  // 예외 메시지를 출력하여 원인 추적
            return result;
        }
    }

}