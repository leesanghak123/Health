package com.sang.health.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserDTO {

	@NotNull(message = "Role cannot be null")
    private String role;
    
    @NotNull(message = "Username cannot be null")
    private String username;
}