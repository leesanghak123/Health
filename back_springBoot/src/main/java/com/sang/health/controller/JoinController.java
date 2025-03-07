package com.sang.health.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sang.health.dto.JoinDto;
import com.sang.health.service.JoinService;

@RestController
@RequestMapping("/api/auth")
public class JoinController {
	
	private final JoinService joinService;
	
	public JoinController(JoinService joinService) {
		this.joinService = joinService;
	}
	
	@PostMapping("/join")
	public ResponseEntity<String> joinProcess(@RequestBody JoinDto joinDto) {
		
		joinService.joinProcess(joinDto);
		return ResponseEntity.ok("가입 성공");
	}
}
