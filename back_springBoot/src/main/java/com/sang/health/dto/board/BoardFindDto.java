package com.sang.health.dto.board;

import java.sql.Timestamp;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BoardFindDto {
	
	private Long id;
	
	@NotEmpty
	private String title;
	
	@NotEmpty
	private String username;
	
	private Timestamp createDate;
	private int count;
	private int likeCnt;
}