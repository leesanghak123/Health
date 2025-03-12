package com.sang.health.jwt;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sang.health.jwt.JWTUtil;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomLogoutHandler implements LogoutHandler {
    
    private final JWTUtil jwtUtil;
    private final RedisUtil redisUtil;
    
    public CustomLogoutHandler(JWTUtil jwtUtil, RedisUtil redisUtil) {
        this.jwtUtil = jwtUtil;
        this.redisUtil = redisUtil;
    }
    
    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // 쿠키에서 값 뺴오기
        String refresh = null;
        String deviceId = null;
        
        Cookie[] cookies = request.getCookies();
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh")) {
                    refresh = cookie.getValue();
                } else if (cookie.getName().equals("deviceId")) {
                    deviceId = cookie.getValue();
                }
            }
        }
        
        // null check
        if (refresh == null || deviceId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        try {
            // check if token is expired
            jwtUtil.isExpired(refresh);
            
            // check if token category is "refresh"
            String category = jwtUtil.getCategory(refresh);
            if (!category.equals("refresh")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // 토큰에서 username 추출
            String username = jwtUtil.getUsername(refresh);
            
            // Redis에서 해당 사용자의 RefreshToken이 존재하는지 확인
            String key = "RT:" + username + deviceId;
            if (!redisUtil.hasKey(key)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            // Redis에서 RefreshToken 삭제
            redisUtil.deleteData(key);
            
            // refresh token 제거
            Cookie cookie = new Cookie("refresh", null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
            
            // DeviceId 제거
            Cookie deviceCookie = new Cookie("deviceId", null);
            deviceCookie.setMaxAge(0);
            deviceCookie.setPath("/");
            response.addCookie(deviceCookie);
            
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }
}