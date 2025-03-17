package com.sang.health.service.board;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sang.health.dto.board.BoardDetailDto;
import com.sang.health.dto.board.BoardFindDto;
import com.sang.health.dto.board.BoardWriteDto;
import com.sang.health.entity.Board;
import com.sang.health.entity.BoardCount;
import com.sang.health.entity.User;
import com.sang.health.redis.RedisUtil;
import com.sang.health.repository.BoardCountRepository;
import com.sang.health.repository.BoardRepository;
import com.sang.health.repository.UserRepository;

@Service
public class BoardService {
	
	private final BoardRepository boardRepository;
	private final UserRepository userRepository;
	private final RedisUtil redisUtil;
	private final BoardCountRepository boardCountRepository;
	
	public BoardService(BoardRepository boardRepository, UserRepository userRepository, RedisUtil redisUtil, BoardCountRepository boardCountRepository) {
		this.boardRepository = boardRepository;
		this.userRepository = userRepository;
		this.redisUtil = redisUtil;
		this.boardCountRepository = boardCountRepository;
	}
	
	@Transactional(readOnly = true)
	public Page<BoardFindDto> 글목록(Pageable pageable) {
		
		Page<Board> boards = boardRepository.findAll(pageable);
		
		return boards.map(board -> {
			int viewCount = redisUtil.getBoardView(board.getId());
			
			return new BoardFindDto(
				board.getId(),
				board.getTitle(),
				board.getUser().getUsername(),
				board.getCreateDate(),
				viewCount,
				board.getLikeCnt()
			);
		});
	}
	
	@Transactional
	public void 글쓰기(BoardWriteDto boardWriteDto, String username) {
		
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
		
		Board board = new Board();
		board.setTitle(boardWriteDto.getTitle());
        board.setContent(boardWriteDto.getContent());
        board.setLikeCnt(0);  // 좋아요 수 초기화
        board.setUser(user); // 연관 사용자 설정
        
        boardRepository.save(board);
        
        BoardCount boardCount = new BoardCount();
        boardCount.setBoardId(board.getId());
        boardCount.setCount(0);  // 초기 조회수 0
        boardCountRepository.save(boardCount);
        
        // Redis에 새 게시글 조회수 초기화
        redisUtil.saveBoardView(board.getId());
	}
	
	@Transactional(readOnly = true)
	public BoardDetailDto 글상세보기(Long id, String username) {
		
		Board board = boardRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
		
		// 조회수 증가
		if(redisUtil.canIncrementView(username, id)) {
			redisUtil.incrementBoardView(id);
		}
		
		// 조회수 조회
		int viewCount = redisUtil.getBoardView(id);
		
		return new BoardDetailDto(
				board.getId(),
				board.getTitle(),
				board.getContent(),
				board.getUser().getUsername(),
				board.getCreateDate(),
				viewCount,
				board.getLikeCnt(),
				false
		);
	}
	
	@Transactional
	public void 글수정(Long id, BoardWriteDto boardWriteDto, String username) {
		
		Board board = boardRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
		
		if (!board.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("본인의 글만 수정할 수 있습니다.");
        }
		
		board.setTitle(boardWriteDto.getTitle());
		board.setContent(boardWriteDto.getContent());
		boardRepository.save(board);
	}
	
	@Transactional
	public void 글삭제(Long id, String username) {
		 
		Board board = boardRepository.findById(id)
	                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

		if (!board.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("본인의 글만 삭제할 수 있습니다.");
        }
		
		boardRepository.delete(board);
		
		// 조회수 엔티티 삭제
	    boardCountRepository.deleteById(id);
	    
		redisUtil.deleteBoardView(id);
	}
}
