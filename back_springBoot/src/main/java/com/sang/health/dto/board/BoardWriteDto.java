package com.sang.health.dto.board;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class BoardWriteDto {

	@NotEmpty(message = "제목은 필수 입력입니다.")
	private String title;
	
	@NotEmpty(message = "내용은 필수 입력입니다.")
	private String content;
}
