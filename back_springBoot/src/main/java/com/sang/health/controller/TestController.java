package com.sang.health.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	
	@GetMapping("/")
	public String Test() {
		return "성공이야";
	}
}
