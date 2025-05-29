package com.sang.health.dto.reply;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReReplyWriteDto {
	
	@NotNull
	private Long replyid;
	
	@NotBlank(message = "제목은 필수 입력입니다.")
	private String content;
}