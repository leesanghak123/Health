package com.sang.health.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sang.health.dto.JoinDto;
import com.sang.health.entity.User;
import com.sang.health.repository.UserRepository;

@Service
public class JoinService {
	
	private final UserRepository userRepository;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	
	public JoinService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
		this.userRepository = userRepository;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
	}
	
	@Transactional
	public void joinProcess(JoinDto joinDto) {
		String username = joinDto.getUsername();
		String password = joinDto.getPassword();
		
		Boolean isExist = userRepository.existsByUsername(username);
		
		// 회원가입이 안된 경우
		if(isExist) {
			return;
		}
		
		User data = new User();
		
		data.setUsername(username);
		data.setPassword(bCryptPasswordEncoder.encode(password));
		data.setRole("ROLE_USER");  // 접두사 ROLE 작성
		
		userRepository.save(data);
	}
}