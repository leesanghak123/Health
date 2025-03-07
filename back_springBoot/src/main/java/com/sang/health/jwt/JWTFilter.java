package com.sang.health.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sang.health.entity.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// OncePerRequestFilter: 요청에 대해 한 번만 동작하는 필터
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;
        String authorization = request.getHeader("Authorization");

        // 헤더에서 JWT 가져오기
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7); // "Bearer " 부분 제거
        } 
        // 헤더에 없으면 쿠키에서 가져오기
        else {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwt".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }

        // JWT가 없으면 필터 체인 계속 진행
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // JWT 만료 여부 확인
        if (jwtUtil.isExpired(token)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT 만료");
            return;
        }

        // JWT에서 사용자 정보 추출 후 인증 처리
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        User user = new User();
        user.setUsername(username);
        user.setRole(role);

        // UsernamePasswordAuthenticationToken: Authentication의 구현체
        Authentication authToken = new UsernamePasswordAuthenticationToken(user, null, List.of(() -> role)); // user, password, role
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}