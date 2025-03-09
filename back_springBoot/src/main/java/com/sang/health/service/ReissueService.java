package com.sang.health.service;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sang.health.entity.Refresh;
import com.sang.health.jwt.JWTUtil;
import com.sang.health.repository.RefreshRepository;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;

@Service
public class ReissueService {
    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    
    public ReissueService(JWTUtil jwtUtil, RefreshRepository refreshRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }
    
    public Map<String, Object> reissueAccessToken(String refreshToken) {
        Map<String, Object> result = new HashMap<>();
        
        // 토큰 만료 확인
        try {
            jwtUtil.isExpired(refreshToken);
        } catch (ExpiredJwtException e) {
            result.put("success", false);
            result.put("message", "refresh token expired");
            return result;
        }
        
        // DB에 저장되어 있는지 확인
//        Boolean isExist = refreshRepository.existsByRefresh(refreshToken);
//        if (!isExist) {
//            result.put("success", false);
//            result.put("message", "invalid refresh token");
//            return result;
//        }
        
        // 토큰 카테고리 확인
        try {
        	System.out.println("확인");
            String category = jwtUtil.getCategory(refreshToken);
            if (!category.equals("refresh")) {
                result.put("success", false);
                result.put("message", "invalid refresh token");
                return result;
            }
            
            // 사용자 정보 추출
            String username = jwtUtil.getUsername(refreshToken);
            String role = jwtUtil.getRole(refreshToken);
            
            // 새로운 Access 토큰 생성 (10분 유효) (현재 6초)
            String newAccessToken = jwtUtil.createJwt("access", username, role, 6000L);
            // Refresh Rotate를 하기 위한 Refresh 생성
            String newRefresh = jwtUtil.createJwt("refresh", username, role, 86400000L);
            
            // Refresh 토큰 저장: 기존의 Refresh 토큰 삭제 후 새 Refresh 토큰 저장
            refreshRepository.deleteByRefresh(refreshToken);
            addRefreshEntity(username, newRefresh, 86400000L);
            
            // 성공 결과 반환
            result.put("success", true);
            result.put("accessToken", newAccessToken);
            result.put("refreshTokenCookie", createCookie("refresh", newRefresh)); // Refresh Token 쿠키
            return result;
        } catch (Exception e) {
            // 기타 예외 처리
            result.put("success", false);
            result.put("message", "token processing error: " + e.getMessage());
            return result;
        }
    }
    
    // 새로운 refreshToken 저장
    private void addRefreshEntity(String username, String refresh, Long expiredMs) {

        Date date = new Date(System.currentTimeMillis() + expiredMs);

        Refresh refreshEntity = new Refresh();
        refreshEntity.setUsername(username);
        refreshEntity.setRefresh(refresh);
        refreshEntity.setExpiration(date.toString());

        refreshRepository.save(refreshEntity);
    }
    
    // Cookie 생성 메서드 (key, jwt)
 	private Cookie createCookie(String key, String value) {

 	    Cookie cookie = new Cookie(key, value);
 	    cookie.setMaxAge(24*60*60);
 	    //cookie.setSecure(true); // https
 	    cookie.setPath("/");
 	    cookie.setHttpOnly(true);

 	    return cookie;
 	}
}