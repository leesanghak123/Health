package com.sang.health.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sang.health.jwt.JWTUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JWTUtil jwtUtil;

    public AuthController(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // 소셜 로그인 후 쿠키 기반으로 JWT 반환
    @PostMapping("/jwt") // Map<키, 값>으로 Cookie에 jwt를 찾아서 추출
    public ResponseEntity<Map<String, String>> getJWTFromCookie(@CookieValue(value = "jwt", required = false) String jwt) {
        if (jwt == null || jwtUtil.isExpired(jwt)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "JWT 만료 또는 없음"));
        }
        return ResponseEntity.ok(Map.of("jwt", jwt));
    }
}
