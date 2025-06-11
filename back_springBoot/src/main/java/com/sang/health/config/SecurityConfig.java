package com.sang.health.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.sang.health.jwt.CustomLogoutHandler;
import com.sang.health.jwt.JWTFilter;
import com.sang.health.jwt.LoginFilter;
import com.sang.health.jwt.OauthCustomSuccessHandler;
import com.sang.health.service.user.CustomOAuth2UserService;
import com.sang.health.util.JWTUtil;
import com.sang.health.util.RedisUtil;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	// AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OauthCustomSuccessHandler oauthCustomSuccessHandler;
    private final CustomLogoutHandler customLogoutHandler;
    private final RedisUtil redisUtil;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil, CustomOAuth2UserService customOAuth2UserService, OauthCustomSuccessHandler oauthCustomSuccessHandler, CustomLogoutHandler customLogoutHandler, RedisUtil redisUtil) {
    	this.authenticationConfiguration = authenticationConfiguration;
    	this.jwtUtil = jwtUtil;
    	this.customOAuth2UserService = customOAuth2UserService;
    	this.oauthCustomSuccessHandler = oauthCustomSuccessHandler;
    	this.customLogoutHandler = customLogoutHandler;
    	this.redisUtil = redisUtil;
    }
    
    // AuthenticationManager Bean 등록
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    	
        return configuration.getAuthenticationManager();
    }
	
	@Bean
    BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }
	
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		// cors 설정
		http.cors((corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                CorsConfiguration configuration = new CorsConfiguration();
                
                // 허용할 port
                configuration.setAllowedOrigins(Arrays.asList(
                	    "http://localhost:8003", // 로컬 개발용
                	    "http://13.209.35.255:8003" // 배포용
                ));
                configuration.setAllowedMethods(Collections.singletonList("*")); // 허용할 method(Get, Post 등)
                configuration.setAllowCredentials(true); // 자격 증명(예: CORS 도메인 쿠키 전송) 허용
                configuration.setAllowedHeaders(Collections.singletonList("*")); // 허용할 헤더
                configuration.setMaxAge(3600L); // 허용을 할 수 있는 시간

				configuration.setExposedHeaders(Arrays.asList("Authorization", "access", "refresh")); // 브라우저가 JS를 통해 읽게 해주는 해더

                return configuration;
            }
        })));
		
		// csrf disable: JWT 방식에서는 필요 X
		http.csrf((auth) -> auth.disable());

		// From 로그인 방식 disable: JWT 방식에서는 필요 X
		http.formLogin((auth) -> auth.disable());

		// http basic 인증 방식 disable: JWT 방식에서는 필요 X
		http.httpBasic((auth) -> auth.disable());

		// Oauth2: Custom 설정을 적용
		http.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(UserInfoEndpointConfig -> UserInfoEndpointConfig
				.userService(customOAuth2UserService))
				.successHandler(oauthCustomSuccessHandler));
		
		// 경로별 인가 작업
		http.authorizeHttpRequests((auth) -> auth
				.requestMatchers("/login", "/api/auth/*", "/reissue", "/board", "/board/api/auth/**").permitAll()
				.requestMatchers("/admin").hasRole("ADMIN")
				.anyRequest().authenticated());  // 다른 경우: 로그인 한 사용자
		
		// jwtFilter: LoginFilter 이전에 수행하도록 등록
		http.addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);
		
		// loginFilter
		http.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, redisUtil), UsernamePasswordAuthenticationFilter.class);
		
		// logoutHandler
		http.logout(logout -> logout
                .logoutUrl("/logout")
                .addLogoutHandler(customLogoutHandler)
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(200);
                })
                .permitAll());
		
		// 세션 설정
		http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();
	}
}