package com.sang.health.dto.board;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BoardDetailDto {

	private Long id;
	private String title;
	private String content;
	private String username;
	private Timestamp createDate;
	private int count;
	private int likeCnt;
	private boolean isLiked; // 추천 여부
}
