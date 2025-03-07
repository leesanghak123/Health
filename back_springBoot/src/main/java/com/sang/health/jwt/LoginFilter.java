package com.sang.health.jwt;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sang.health.dto.CustomUserDetails;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// UsernamePasswordAuthenticationFilter 상속
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
	
	private final AuthenticationManager authenticationManager;
	private final JWTUtil jwtUtil;
	
	public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
	}

	// UsernamePasswordAuthenticationFilter로부터 상속 받은 것을 재정의(Override)
	// return 타입은 Authentication
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

		try {
		// 클라이언트 요청에서 username, password 추출
		// JSON 형식의 데이터를 읽고 파싱
        ObjectMapper objectMapper = new ObjectMapper(); // JSON 데이터 읽기
        Map<String, String> loginData = objectMapper.readValue(request.getInputStream(), Map.class);

        // JSON에서 username과 password 추출
        String username = loginData.get("username");
        String password = loginData.get("password");
		
        // 스프링 시큐리티에서 username과 password를 검증하기 위해서는 token에 담아야 함
        // UsernamePasswordAuthenticationToken : AuthenticationManager에게 주는 DTO 개념
		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

		// token에 담은 검증을 위한 AuthenticationManager로 전달
		return authenticationManager.authenticate(authToken);
		} catch (IOException e) {
	        throw new AuthenticationServiceException("Error parsing login request");
	    }
	}

	// 로그인 성공시 실행하는 메소드 (여기서 JWT를 발급하면 됨)
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {

		CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        // Role 값 뽑아내기
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();

        String token = jwtUtil.createJwt(username, role, 60*60*10L);

        // 접두사 (HTTP 기본 양식)
        response.addHeader("Authorization", "Bearer " + token);
		
        // SecurityContextHolder에 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	// 로그인 실패시 실행하는 메소드
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {

		// 인증되지 않은 사용자
		response.setStatus(401);
	}
}