package com.sang.health.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JwtTestController {
	@GetMapping("/test")
	public String Test() {
		return "Sub Test";
	}
}
