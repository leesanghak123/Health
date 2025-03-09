package com.sang.health.jwt;

import java.io.IOException;
import java.sql.Date;
import java.util.Collection;
import java.util.Iterator;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.sang.health.dto.CustomOAuth2User;
import com.sang.health.entity.Refresh;
import com.sang.health.repository.RefreshRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OauthCustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JWTUtil jwtUtil;
	private RefreshRepository refreshRepository;

	public OauthCustomSuccessHandler(JWTUtil jwtUtil, RefreshRepository refreshRepository) {
		this.jwtUtil = jwtUtil;
		this.refreshRepository = refreshRepository;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

		String username = authentication.getName();
		
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
	    Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
	    GrantedAuthority auth = iterator.next();
	    String role = auth.getAuthority();

	    // 토큰 생성 (refresh Token만)
	    String refresh = jwtUtil.createJwt("refresh", username, role, 86400000L); // 24시간

	    //Refresh 토큰 저장
	    addRefreshEntity(username, refresh, 86400000L);
	    
		// JWT를 HttpOnly 쿠키에 담아서 전달 (쿠키 이름 "jwt" 유지)
		Cookie cookie = new Cookie("refresh", refresh);
		cookie.setHttpOnly(true); // 자바스크립트에서 접근 불가
		// cookie.setSecure(true); // HTTPS에서만 전송
		cookie.setPath("/"); // 전체 도메인에서 접근 가능
		cookie.setMaxAge(60 * 60 * 10); // 10시간(줄이자)

		response.addCookie(cookie); // 쿠키를 응답에 추가
		response.sendRedirect("http://localhost:8003/login?social=true"); // social 파라미터 추가
	}
	
	private void addRefreshEntity(String username, String refresh, Long expiredMs) {

	    Date date = new Date(System.currentTimeMillis() + expiredMs);

	    Refresh refreshEntity = new Refresh();
	    refreshEntity.setUsername(username);
	    refreshEntity.setRefresh(refresh);
	    refreshEntity.setExpiration(date.toString());

	    refreshRepository.save(refreshEntity);
	}
}