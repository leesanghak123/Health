package com.sang.health.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sang.health.dto.JoinDto;
import com.sang.health.service.JoinService;

@RestController
public class JoinController {
	
	private final JoinService joinService;
	
	public JoinController(JoinService joinService) {
		this.joinService = joinService;
	}
	
	@PostMapping("/join")
	public String joinProcess(JoinDto joinDto) {
		joinService.joinProcess(joinDto);
		return "ok";
	}
}
