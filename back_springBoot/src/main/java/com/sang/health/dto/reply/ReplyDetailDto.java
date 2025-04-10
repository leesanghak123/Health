package com.sang.health.dto.reply;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReplyDetailDto {

	private Long id;
	private String content;
	private String username;
	private Timestamp createDate;
}