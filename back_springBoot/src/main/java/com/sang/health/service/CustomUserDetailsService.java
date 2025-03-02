package com.sang.health.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sang.health.dto.CustomUserDetails;
import com.sang.health.entity.User;
import com.sang.health.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService{

	private final UserRepository userRepository;
	
	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		// DB에서 조회
		User userData = userRepository.findByUsername(username);
		
		if(userData != null) {
			// UserDetails에 담아서 return 하면 AuthenticationManager가 검증함
			return new CustomUserDetails(userData);
		}
		
		return null;
	}
}