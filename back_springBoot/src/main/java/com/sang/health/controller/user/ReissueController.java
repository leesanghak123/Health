package com.sang.health.controller.user;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sang.health.service.user.ReissueService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class ReissueController {
    private final ReissueService reissueService;
    
    public ReissueController(ReissueService reissueService) {
        this.reissueService = reissueService;
    }
    
    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        
    	// 쿠키에서 refresh 토큰 추출
        String refreshToken = extractRefreshTokenFromCookie(request);
        String deviceId = extractDeviceIdFromCookie(request);
        
        if (refreshToken == null) {
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }
        
        if (deviceId == null) {
            return new ResponseEntity<>("deviceId null", HttpStatus.BAD_REQUEST);
        }
        
        // 서비스에서 토큰 재발급 요청
        Map<String, Object> result = reissueService.reissueAccessToken(refreshToken, deviceId);
        // 성공 여부 확인
        boolean isSuccess = (boolean) result.get("success");
        System.out.println(isSuccess);
        
        if (isSuccess) {
            String newAccessToken = (String) result.get("accessToken");
            response.setHeader("access", newAccessToken);
            
            // 새로운 Refresh Token을 쿠키로 설정
            Cookie refreshTokenCookie = (Cookie) result.get("refreshTokenCookie");
            response.addCookie(refreshTokenCookie);
            
            // 새로운 deviceId를 쿠키로 설정
            Cookie newDeviceIdCookie = (Cookie) result.get("newDeviceIdCookie");
            response.addCookie(newDeviceIdCookie);
            
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            String errorMessage = (String) result.get("message");
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        }
    }
    
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                return cookie.getValue();
            }
        }
        return null;
    }
    
    private String extractDeviceIdFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("deviceId")) {
                return cookie.getValue();
            }
        }
        return null;
    }
}