package com.sang.health.controller.reply;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.sang.health.dto.reply.ReReplyWriteDto;
import com.sang.health.dto.reply.ReplyWriteDto;
import com.sang.health.service.reply.ReplyService;
import com.sang.health.util.JWTUtil;

import jakarta.validation.Valid;

@RestController
public class ReplyController {

	private final ReplyService replyService;
	private final JWTUtil jwtUtil;
	
	public ReplyController(ReplyService replyService, JWTUtil jwtUtil) {
		this.replyService = replyService;
		this.jwtUtil = jwtUtil;
	}
	
	@PostMapping("/reply")
	public ResponseEntity<?> replyWrite(@Valid @RequestBody ReplyWriteDto replyWriteDto, @RequestHeader("access") String token) { // boardid
		
		String username = jwtUtil.getUsername(token);
		replyService.댓글작성(replyWriteDto, username);
		return ResponseEntity.status(HttpStatus.OK).body("댓글 작성 완료");
	}
	
	@PostMapping("/rereply")
	public ResponseEntity<?> rereplyWrite(@Valid @RequestBody ReReplyWriteDto rereplyWriteDto, @RequestHeader("access") String token) { // replyid
		
		String username = jwtUtil.getUsername(token);
		replyService.대댓글작성(rereplyWriteDto, username);
		return ResponseEntity.status(HttpStatus.OK).body("대댓글 작성 완료");
	}
	
	@DeleteMapping("/reply/delete/{id}")
	public ResponseEntity<?> replyDelete(@PathVariable Long id, @RequestHeader("access") String token) {
		
		String username = jwtUtil.getUsername(token);
		
		replyService.댓글삭제(id, username);
		return ResponseEntity.status(HttpStatus.OK).body("댓글 삭제 완료");
	}
	
	@DeleteMapping("/rereply/delete/{id}")
	public ResponseEntity<?> rereplyDelete(@PathVariable Long id, @RequestHeader("access") String token) {
		
		String username = jwtUtil.getUsername(token);
		
		replyService.대댓글삭제(id, username);
		return ResponseEntity.status(HttpStatus.OK).body("대댓글 삭제 완료");
	}
}
