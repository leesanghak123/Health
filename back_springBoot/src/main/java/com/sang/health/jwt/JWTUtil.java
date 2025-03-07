package com.sang.health.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;

// @Component : Spring IoC Container에서 클래스를 관리할 수 있게 해준다.
// Service, Controller 등은 Component의 하위 어노테이션
// @Bean은 메소드를 Spring IoC Container에서 관리할 수 있게 하는 것
// JWT 생성 및 검증하는 Util 클래스
@Component
public class JWTUtil {

	private SecretKey secretKey;

	// @Value : application.yml에 저장되어 있는 Data 들고오기
    public JWTUtil(@Value("${spring.jwt.secret}")String secret) {

    	// Key 객체를 암호화
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    // username 검증
    public String getUsername(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }
    
    // role 검증
    public String getRole(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }

    // 만료시간 검증
    public Boolean isExpired(String token) {

        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    // Token 생성
    public String createJwt(String username, String role, Long expiredMs) {
        String jwt = Jwts.builder()
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis())) // 현재 발행 시간
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
        return jwt;
    }
}