package com.sang.health;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackSpringBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackSpringBootApplication.class, args);
	}

}
