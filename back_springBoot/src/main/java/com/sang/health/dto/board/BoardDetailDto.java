package com.sang.health.dto.board;

import java.sql.Timestamp;
import java.util.List;

import com.sang.health.dto.reply.ReReplyDetailDto;
import com.sang.health.dto.reply.ReplyDetailDto;

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
	private List<ReplyDetailDto> reply;
	private List<ReReplyDetailDto> reReply;
}
