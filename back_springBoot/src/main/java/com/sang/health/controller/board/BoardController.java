package com.sang.health.controller.board;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sang.health.dto.board.BoardDetailDto;
import com.sang.health.dto.board.BoardFindDto;
import com.sang.health.dto.board.BoardWriteDto;
import com.sang.health.jwt.JWTUtil;
import com.sang.health.service.board.BoardService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/board")
public class BoardController {
	
	private final JWTUtil jwtUtil;
	private final BoardService boardService;
	
	public BoardController(JWTUtil jwtUtil, BoardService boardService) {
		this.boardService = boardService;
		this.jwtUtil = jwtUtil;
	}
	
	@GetMapping("")
	public ResponseEntity<?> findPage(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
		
		Page<BoardFindDto> boards = boardService.글목록(pageable);
		
		Map<String, Object> response = new HashMap<>();
	    response.put("content", boards.getContent());
	    response.put("pageNumber", boards.getNumber());
	    response.put("pageSize", boards.getSize());
	    response.put("totalElements", boards.getTotalElements());
	    response.put("totalPages", boards.getTotalPages());
		
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/write")
	public ResponseEntity<?> write(@Valid @RequestBody BoardWriteDto boardWriteDto, @RequestHeader("access") String token) {
		
		String username = jwtUtil.getUsername(token);
		
		boardService.글쓰기(boardWriteDto, username);
		return ResponseEntity.status(HttpStatus.CREATED).body("글 작성 완료");
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<?> Detail(@PathVariable Long id, @RequestHeader("access") String token) {
		
		String username = jwtUtil.getUsername(token);
		
		BoardDetailDto boardDetail = boardService.글상세보기(id, username);
		
		if (boardDetail == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 게시글을 찾을 수 없습니다.");
        }
		return ResponseEntity.ok(boardDetail);
	}
	
	@PatchMapping("/update/{id}")
	public ResponseEntity<?> update(@Valid @PathVariable Long id, @RequestBody BoardWriteDto boardWriteDto, @RequestHeader("access") String token) {
		
		String username = jwtUtil.getUsername(token);
		
		boardService.글수정(id, boardWriteDto, username);
		return ResponseEntity.status(HttpStatus.OK).body("글 수정 완료");
	}
	
	@DeleteMapping("/delete/{id}")
	public ResponseEntity<?> delete(@PathVariable Long id, @RequestHeader("access") String token) {
		
		String username = jwtUtil.getUsername(token);
		
		boardService.글삭제(id, username);
		return ResponseEntity.status(HttpStatus.OK).body("글 삭제 완료");
	}
	
	@PostMapping("/{id}/like")
	public ResponseEntity<?> likeBoard(@PathVariable Long id, @RequestHeader("access") String token) {
		
		String username = jwtUtil.getUsername(token);
		
		boardService.추천추가(id, username);
		return ResponseEntity.status(HttpStatus.OK).body("추천 완료");
	}
	
	@DeleteMapping("{id}/like")
	public ResponseEntity<?> unlikeBoard(@PathVariable Long id, @RequestHeader("access") String token) {
		
		String username = jwtUtil.getUsername(token);
		
		boardService.추천삭제(id, username);
		return ResponseEntity.status(HttpStatus.OK).body("추천 삭제 완료");
	}
	
	@GetMapping("/api/auth/search/title")
	public ResponseEntity<?> searchByTitle(
			@RequestParam String title, 
			@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
		Page<BoardFindDto> results = boardService.제목찾기(title, pageable);
		
		Map<String, Object> response = new HashMap<>();
	    response.put("content", results.getContent());
	    response.put("pageNumber", results.getNumber());
	    response.put("pageSize", results.getSize());
	    response.put("totalElements", results.getTotalElements());
	    response.put("totalPages", results.getTotalPages());
	    
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/api/auth/search/content")
	public ResponseEntity<?> searchByContent(
			@RequestParam String content, 
			@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
		Page<BoardFindDto> results = boardService.내용찾기(content, pageable);

		Map<String, Object> response = new HashMap<>();
	    response.put("content", results.getContent());
	    response.put("pageNumber", results.getNumber());
	    response.put("pageSize", results.getSize());
	    response.put("totalElements", results.getTotalElements());
	    response.put("totalPages", results.getTotalPages());
	    
		return ResponseEntity.ok(response);
	}
	
	@GetMapping("/api/auth/search")
	public ResponseEntity<?> searchByTitleOrContent(
			@RequestParam String keyword, 
			@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
		Page<BoardFindDto> results = boardService.제목내용찾기(keyword, pageable);

		Map<String, Object> response = new HashMap<>();
	    response.put("content", results.getContent());
	    response.put("pageNumber", results.getNumber());
	    response.put("pageSize", results.getSize());
	    response.put("totalElements", results.getTotalElements());
	    response.put("totalPages", results.getTotalPages());
	    
		return ResponseEntity.ok(response);
	}
}
