package com.sang.health.config;

import java.util.Collections;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// MVC 레벨 Cors 설정 (Controller로 들어오는 부분)
@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        
    	// 모든 Controller 부분에서 허용할 port 번호 8003 설정
    	corsRegistry.addMapping("/**")
        	.allowedOrigins("http://localhost:8003");
    }
}