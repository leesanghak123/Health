package com.sang.health.dto.board;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoardWithUsername {
	
	@NotBlank
	private Long id;
	
	@NotBlank
    private String title;
	
	@NotBlank
    private String content;
	
	@NotBlank
    private java.sql.Timestamp createDate;
	
	@NotBlank
    private String username;
}
