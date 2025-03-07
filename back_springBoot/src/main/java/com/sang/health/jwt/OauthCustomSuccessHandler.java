package com.sang.health.jwt;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.sang.health.dto.CustomOAuth2User;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OauthCustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JWTUtil jwtUtil;

  public OauthCustomSuccessHandler(JWTUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
    CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();
    String username = customUserDetails.getName();

    // JWT 생성
    String jwt = jwtUtil.createJwt(username, "ROLE_USER", 60 * 60 * 10L);

    // JWT를 HttpOnly 쿠키에 담아서 전달 (쿠키 이름 "jwt" 유지)
    Cookie cookie = new Cookie("jwt", jwt);
    cookie.setHttpOnly(true); // 자바스크립트에서 접근 불가
    //cookie.setSecure(true); // HTTPS에서만 전송
    cookie.setPath("/"); // 전체 도메인에서 접근 가능
    cookie.setMaxAge(60 * 60 * 10); // 10시간(줄이자)

    response.addCookie(cookie); // 쿠키를 응답에 추가
    response.sendRedirect("http://localhost:8003/login?social=true"); // social 파라미터 추가
  }
}