package com.sang.health.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sang.health.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
	// User의 존재 파악 (exists) (회원가입 시)
	Boolean existsByUsername(String username);
	
	// (로그인 시) & (소셜 로그인)
	User findByUsername(String username);
}