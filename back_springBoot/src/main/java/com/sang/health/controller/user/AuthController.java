package com.sang.health.controller.user;

import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sang.health.util.JWTUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final JWTUtil jwtUtil;
    
    public AuthController(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }
    
    // 소셜 로그인 후 refresh 토큰으로 access 토큰 발급
    @GetMapping("/jwt")
    public ResponseEntity<?> getAccessToken(@CookieValue(value = "refresh", required = false) String refreshToken) {
        // refresh 토큰 유효성 검사
        if (refreshToken == null || jwtUtil.isExpired(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh 토큰이 만료되었거나 없습니다."));
        }

        try {
            // refresh 토큰에서 사용자 정보 추출
            String username = jwtUtil.getUsername(refreshToken);
            String role = jwtUtil.getRole(refreshToken);

            // access 토큰 생성 (10분) (현재 6초)
            String accessToken = jwtUtil.createJwt("access", username, role, 6000L);

            // 응답 헤더에 token 추가
            HttpHeaders headers = new HttpHeaders();
            headers.set("access", accessToken);
            headers.set("Access-Control-Expose-Headers", "access");

            return new ResponseEntity<>(Map.of("message", "토큰이 성공적으로 발급되었습니다."), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "토큰 생성 중 오류가 발생했습니다."));
        }
    }
}