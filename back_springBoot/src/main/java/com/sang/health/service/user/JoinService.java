package com.sang.health.service.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sang.health.dto.user.JoinDto;
import com.sang.health.entity.user.User;
import com.sang.health.repository.user.UserRepository;
import com.sang.health.util.HtmlSanitizerUtil;

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
		// Sanitizer로 정제
		String sanitizedUsername = HtmlSanitizerUtil.sanitize(joinDto.getUsername());
		String sanitizedEmail = HtmlSanitizerUtil.sanitize(joinDto.getEmail());
		String password = joinDto.getPassword();
		
		Boolean isExist = userRepository.existsByUsername(sanitizedUsername);
		
		// 회원가입이 된 경우
		if(isExist) {
			return;
		}
		
		// 팩토리 메서드를 사용하여 User 객체 생성
        User user = User.createLocalUser(
        	sanitizedUsername,
        	sanitizedEmail,
            bCryptPasswordEncoder.encode(password),
            "ROLE_USER"
        );
		
		userRepository.save(user);
	}
}