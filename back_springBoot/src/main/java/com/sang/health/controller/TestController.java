package com.sang.health.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	//@CrossOrigin(origins = "http://localhost:8003")
	@GetMapping("/")
	public String Test() {
		return "Main Test";
	}
}
