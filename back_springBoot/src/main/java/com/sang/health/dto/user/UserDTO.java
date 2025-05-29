package com.sang.health.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDTO {

	@NotBlank(message = "Role cannot be null")
    private String role;
    
    @NotBlank(message = "Username cannot be null")
    private String username;
}