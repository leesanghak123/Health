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
import com.sang.health.entity.BoardLikeTotal;
import com.sang.health.entity.User;
import com.sang.health.redis.RedisUtil;
import com.sang.health.repository.BoardCountRepository;
import com.sang.health.repository.BoardLikeRepository;
import com.sang.health.repository.BoardLikeTotalRepository;
import com.sang.health.repository.BoardRepository;
import com.sang.health.repository.UserRepository;

@Service
public class BoardService {
	
	private final BoardRepository boardRepository;
	private final UserRepository userRepository;
	private final RedisUtil redisUtil;
	private final BoardCountRepository boardCountRepository;
	private final BoardLikeTotalRepository boardLikeTotalRepository;
	private final BoardLikeRepository boardLikeRepository;
	
	public BoardService(BoardRepository boardRepository, UserRepository userRepository, RedisUtil redisUtil, BoardCountRepository boardCountRepository, BoardLikeTotalRepository boardLikeTotalRepository, BoardLikeRepository boardLikeRepository) {
		this.boardRepository = boardRepository;
		this.userRepository = userRepository;
		this.redisUtil = redisUtil;
		this.boardCountRepository = boardCountRepository;
		this.boardLikeTotalRepository = boardLikeTotalRepository;
		this.boardLikeRepository = boardLikeRepository;
	}
	
	@Transactional(readOnly = true)
	public Page<BoardFindDto> 글목록(Pageable pageable) {
		
		Page<Board> boards = boardRepository.findAll(pageable);
		
		// Page 라서 하나씩 들고옴
		return boards.map(board -> {
			int viewCount = redisUtil.getBoardView(board.getId()); // 조회수
			int likeCount = redisUtil.getBoardLikeTotal(board.getId()); // 추천수
			
			return new BoardFindDto(
				board.getId(),
				board.getTitle(),
				board.getUser().getUsername(),
				board.getCreateDate(),
				viewCount,
				likeCount
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
        board.setUser(user); // 연관 사용자 설정
        
        boardRepository.save(board);
        
        // Redis에 새 게시글 조회수 초기화
        redisUtil.saveBoardView(board.getId());
        
        // Redis에 새 게시글 추천수 초기화
        redisUtil.saveBoardLikeTotal(board.getId());
        
        // DB에 조회수 초기화 (0으로)
        BoardCount boardCount = new BoardCount();
        boardCount.setBoardId(board.getId());
        boardCount.setCount(0);
        boardCountRepository.save(boardCount);
        
        // DB에 추천수 초기화 (0으로)
        BoardLikeTotal boardLikeTotal = new BoardLikeTotal();
        boardLikeTotal.setBoardId(board.getId());
        boardLikeTotal.setLikeTotal(0);
        boardLikeTotalRepository.save(boardLikeTotal);
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
		
		// 추천수 조회
		int likeCount = redisUtil.getBoardLikeTotal(id);
		
		// 추천수 유무
		boolean isLiked = redisUtil.getBoardLike(id, username);
		
		return new BoardDetailDto(
				board.getId(),
				board.getTitle(),
				board.getContent(),
				board.getUser().getUsername(),
				board.getCreateDate(),
				viewCount,
				likeCount,
				isLiked
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
		
	    boardCountRepository.deleteById(id); // 조회수 엔티티 삭제
	    boardLikeTotalRepository.deleteById(id); // 전체 추천수 엔티티 삭제
	    boardLikeRepository.deleteById(id); // 추천 user 삭제
	    
		redisUtil.deleteBoardView(id);
		redisUtil.deleteBoardLike(id);
		redisUtil.deleteBoardLikeTotal(id);
		redisUtil.deletecanIncrementView(username, id);
	}
	
	public void 추천추가(Long id, String username) {
		
		if (!redisUtil.getBoardLike(id, username)) {
            redisUtil.incrementBoardLike(id);
            redisUtil.createBoardLike(id, username);
        }
	}
	
	public void 추천삭제(Long id, String username) {
		
		if (redisUtil.getBoardLike(id, username)) {
            redisUtil.decrementBoardLike(id);
            redisUtil.deleteBoardLike(id, username);
        }
	}
}
