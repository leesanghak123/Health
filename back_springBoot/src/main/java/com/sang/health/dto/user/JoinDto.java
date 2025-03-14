package com.sang.health.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class JoinDto {
	
	    @NotNull(message = "Username cannot be null")
	    private String username;
	    
	    @NotNull(message = "Password cannot be null")
	    private String password;
	    
	    @Email(message = "Invalid email format")
	    private String email;
}