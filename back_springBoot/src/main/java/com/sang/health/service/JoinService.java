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
		String email = joinDto.getEmail();
		
		Boolean isExist = userRepository.existsByUsername(username);
		
		// 회원가입이 된 경우
		if(isExist) {
			return;
		}
		
		// 팩토리 메서드를 사용하여 User 객체 생성
        User user = User.createLocalUser(
            username,
            email,
            bCryptPasswordEncoder.encode(password),
            "ROLE_USER"
        );
		
		userRepository.save(user);
	}
}