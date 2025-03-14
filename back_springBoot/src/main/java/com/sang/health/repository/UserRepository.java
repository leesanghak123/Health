package com.sang.health.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sang.health.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	
	Optional<User> findByUsername(String username);
	
	Boolean existsByUsername(String username);
}