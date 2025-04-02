package com.sang.health.dto.reply;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReplyWriteDto {
	
	@NotNull
	private Long boardid;
	
	@NotEmpty(message = "제목은 필수 입력입니다.")
	private String content;
}
