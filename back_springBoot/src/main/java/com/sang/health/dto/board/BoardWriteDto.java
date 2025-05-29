package com.sang.health.dto.board;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BoardWriteDto {

	@NotBlank(message = "제목은 필수 입력입니다.")
	private String title;
	
	@NotBlank(message = "내용은 필수 입력입니다.")
	private String content;
}