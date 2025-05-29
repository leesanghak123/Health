package com.sang.health.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class JoinDto {
	
	    @NotBlank(message = "Username cannot be null")
	    private String username;
	    
	    @NotBlank(message = "Password cannot be null")
	    private String password;
	    
	    @Email(message = "Invalid email format")
	    @NotBlank
	    private String email;
}