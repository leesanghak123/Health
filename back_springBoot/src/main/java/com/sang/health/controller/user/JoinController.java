package com.sang.health.controller.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sang.health.dto.user.JoinDto;
import com.sang.health.service.user.JoinService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class JoinController {
	
	private final JoinService joinService;
	
	public JoinController(JoinService joinService) {
		this.joinService = joinService;
	}
	
	@PostMapping("/join")
	public ResponseEntity<String> joinProcess(@Valid @RequestBody JoinDto joinDto) {
		
		joinService.joinProcess(joinDto);
		return ResponseEntity.ok("가입 성공");
	}
}
