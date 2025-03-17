package com.sang.health.jwt;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sang.health.dto.user.CustomUserDetails;
import com.sang.health.entity.User;

import io.jsonwebtoken.ExpiredJwtException;
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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    	System.out.println("JWTFilter 요청 URI: " + request.getRequestURI());
        
    	// "/reissue" 요청은 필터에서 제외
        if (request.getRequestURI().equals("/reissue")) {
            filterChain.doFilter(request, response);
            return;
        }
    	
    	// 토큰 획득 시도 (헤더 또는 쿠키에서)
        String token = extractToken(request);

        // 토큰이 없다면 다음 필터로 넘김 (권한이 필요없는 부분 요청일 수도 있기 때문)
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 토큰 유효성 및 만료 확인
            jwtUtil.isExpired(token);
            
            // 토큰 종류 확인 (access 또는 refresh)
            String category = jwtUtil.getCategory(token);
            
            // 엑세스 토큰인 경우 바로 인증 처리
            if (category.equals("access")) {
                processToken(token, request, response, filterChain);
            } 
            // 리프레시 토큰인 경우 (소셜 로그인 쿠키에서 전달된 경우)
            else if (category.equals("refresh")) {
                // API 엔드포인트가 /api/auth/jwt인 경우에만 refresh 토큰을 처리
                // 이 엔드포인트는 새 액세스 토큰을 발급하는 용도로만 사용
                if (request.getRequestURI().equals("/api/auth/jwt")) {
                    processToken(token, request, response, filterChain);
                } else {
                    // 다른 API에서는 refresh 토큰으로 직접 인증 불가
                    PrintWriter writer = response.getWriter();
                    writer.print("Access token required");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            }
            // 토큰 종류가 올바르지 않은 경우
            else {
                PrintWriter writer = response.getWriter();
                writer.print("Invalid token category");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
        } catch (ExpiredJwtException e) {
            // 토큰 만료 시 특정 에러 코드와 JSON 응답 반환
        	System.out.println("토큰 만료 예외 발생: " + e.getMessage());
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            PrintWriter writer = response.getWriter(); // 특정 에러 반환 시 프론트에서 /reissue를 하기 위함
            writer.print("{\"error\":\"TOKEN_EXPIRED\",\"message\":\"Access token has expired\"}");
            return;
        } catch (Exception e) {
            // 기타 예외 발생 시
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            PrintWriter writer = response.getWriter();
            writer.print("{\"error\":\"INVALID_TOKEN\",\"message\":\"Invalid token\"}");
            return;
        }
    }
    
    // 토큰 추출 메서드 (헤더 또는 쿠키에서)
    private String extractToken(HttpServletRequest request) {
        String token = null;
        
        // 1. access 헤더에서 확인 (기존 로직)
        token = request.getHeader("access");
        if (token != null) {
            return token;
        }
        
        // 2. 쿠키에서 확인
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // access 토큰 쿠키 확인
                if ("access".equals(cookie.getName())) {
                    return cookie.getValue();
                }
                // refresh 토큰 쿠키 확인 (소셜 로그인 시)
                else if ("refresh".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }
    
    // 토큰 처리 및 인증 설정 메서드
    private void processToken(String token, HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // username, role 값을 획득
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);
        
        // 사용자 정보로 인증 객체 생성
        User userEntity = new User();
        userEntity.setUsername(username);
        userEntity.setRole(role);
        CustomUserDetails customUserDetails = new CustomUserDetails(userEntity);
        
        // 인증 객체 설정
        Authentication authToken = new UsernamePasswordAuthenticationToken(
            customUserDetails, 
            null, 
            customUserDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);
        
        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }
}