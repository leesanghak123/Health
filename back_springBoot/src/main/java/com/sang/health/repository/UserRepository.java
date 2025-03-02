package com.sang.health.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sang.health.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	
	// User의 존재 파악 (exists) (회원가입 시)
	Boolean existsByUsername(String username);
	
	// (로그인 시)
	User findByUsername(String username);
}