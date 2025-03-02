package com.sang.health.config;

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

import com.sang.health.jwt.JWTFilter;
import com.sang.health.jwt.JWTUtil;
import com.sang.health.jwt.LoginFilter;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	// AuthenticationManager가 인자로 받을 AuthenticationConfiguraion 객체 생성자 주입
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JWTUtil jwtUtil) {
    	this.authenticationConfiguration = authenticationConfiguration;
    	this.jwtUtil = jwtUtil;
    }
    
    // AuthenticationManager Bean 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
    	
        return configuration.getAuthenticationManager();
    }
	
	@Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		// cors 설정
		http.cors((corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {

            @Override
            public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(Collections.singletonList("http://localhost:8003")); // 허용할 port
                configuration.setAllowedMethods(Collections.singletonList("*")); // 허용할 method(Get, Post 등)
                configuration.setAllowCredentials(true); // 자격 증명(예: 쿠키, 인증 헤더) 허용
                configuration.setAllowedHeaders(Collections.singletonList("*")); // 허용할 헤더
                configuration.setMaxAge(3600L); // 허용을 할 수 있는 시간

				configuration.setExposedHeaders(Collections.singletonList("Authorization")); // Authorization 헤더 허용

                return configuration;
            }
        })));
		
		// csrf disable: JWT 방식에서는 필요 X
		http.csrf((auth) -> auth.disable());

		// From 로그인 방식 disable: JWT 방식에서는 필요 X
		http.formLogin((auth) -> auth.disable());

		// http basic 인증 방식 disable: JWT 방식에서는 필요 X
		http.httpBasic((auth) -> auth.disable());

		// 경로별 인가 작업
		http.authorizeHttpRequests((auth) -> auth
				.requestMatchers("/login", "/", "/join").permitAll()
				.requestMatchers("/admin").hasRole("ADMIN")
				.anyRequest().authenticated());  // 다른 경우: 로그인 한 사용자
		
		// LoginFilter 이전에 수행하도록 등록
		http.addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);
		
		http.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class);

		// 세션 설정
		http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();
	}
}