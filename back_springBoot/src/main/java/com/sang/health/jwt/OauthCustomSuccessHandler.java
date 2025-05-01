package com.sang.health.jwt;

import java.io.IOException;
import java.util.UUID;
import java.util.Collection;
import java.util.Iterator;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.sang.health.util.JWTUtil;
import com.sang.health.util.RedisUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

@Component
public class OauthCustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JWTUtil jwtUtil;
	private final RedisUtil redisUtil;

	public OauthCustomSuccessHandler(JWTUtil jwtUtil, RedisUtil redisUtil) {
		this.jwtUtil = jwtUtil;
		this.redisUtil = redisUtil;
	}

	@Override
	@Transactional
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

		String username = authentication.getName();
		
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
	    Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
	    GrantedAuthority auth = iterator.next();
	    String role = auth.getAuthority();
	    
	    String deviceId = UUID.randomUUID().toString();

	    // 토큰 생성 (refresh Token만)
	    String refresh = jwtUtil.createJwt("refresh", username, role, 86400000L);
	    redisUtil.saveRefreshToken(username+deviceId, refresh, 86400000L);
	    response.addCookie(jwtUtil.createDeviceIdCookie(deviceId));

	    response.addCookie(jwtUtil.createRefreshTokenCookie(refresh));
		response.sendRedirect("http://localhost:8003/login?social=true"); // social 파라미터 추가
	}
}