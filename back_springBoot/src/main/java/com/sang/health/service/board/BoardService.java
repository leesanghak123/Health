package com.sang.health.service.board;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sang.health.dto.board.BoardDetailDto;
import com.sang.health.dto.board.BoardFindDto;
import com.sang.health.dto.board.BoardWriteDto;
import com.sang.health.dto.reply.ReReplyDetailDto;
import com.sang.health.dto.reply.ReplyDetailDto;
import com.sang.health.entity.board.Board;
import com.sang.health.entity.board.BoardCount;
import com.sang.health.entity.board.BoardES;
import com.sang.health.entity.board.BoardLikeTotal;
import com.sang.health.entity.reply.ReReply;
import com.sang.health.entity.reply.Reply;
import com.sang.health.entity.user.User;
import com.sang.health.repository.board.BoardCountRepository;
import com.sang.health.repository.board.BoardESRepository;
import com.sang.health.repository.board.BoardLikeRepository;
import com.sang.health.repository.board.BoardLikeTotalRepository;
import com.sang.health.repository.board.BoardRepository;
import com.sang.health.repository.reply.ReReplyRepositoy;
import com.sang.health.repository.reply.ReplyRepository;
import com.sang.health.repository.user.UserRepository;
import com.sang.health.util.RedisUtil;

@Service
public class BoardService {
	
	private final BoardRepository boardRepository;
	private final UserRepository userRepository;
	private final RedisUtil redisUtil;
	private final BoardCountRepository boardCountRepository;
	private final BoardLikeTotalRepository boardLikeTotalRepository;
	private final BoardLikeRepository boardLikeRepository;
	private final BoardESRepository boardESRepository;
	private final ReplyRepository replyRepository;
	private final ReReplyRepositoy reReplyRepositoy;
	
	public BoardService(BoardRepository boardRepository, UserRepository userRepository, RedisUtil redisUtil, BoardCountRepository boardCountRepository, 
			BoardLikeTotalRepository boardLikeTotalRepository, BoardLikeRepository boardLikeRepository, BoardESRepository boardESRepository,
			ReplyRepository replyRepository, ReReplyRepositoy reReplyRepositoy) {
		this.boardRepository = boardRepository;
		this.userRepository = userRepository;
		this.redisUtil = redisUtil;
		this.boardCountRepository = boardCountRepository;
		this.boardLikeTotalRepository = boardLikeTotalRepository;
		this.boardLikeRepository = boardLikeRepository;
		this.boardESRepository = boardESRepository;
		this.replyRepository = replyRepository;
		this.reReplyRepositoy = reReplyRepositoy;
	}
	
	@Transactional(readOnly = true)
	public Page<BoardFindDto> 글목록(Pageable pageable) {
		
		Page<Board> boards = boardRepository.findAll(pageable);
		
		// Page 라서 하나씩 들고옴
		return boards.map(board -> {
			int viewCount = redisUtil.getBoardView(board.getId()); // 조회수
			int likeCount = redisUtil.getBoardLikeTotal(board.getId()); // 추천수
			
			return new BoardFindDto( // map 내부 람다함수 리턴
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
		
		// DB 저장
		Board board = new Board();
		board.setTitle(boardWriteDto.getTitle());
        board.setContent(boardWriteDto.getContent());
        board.setUser(user); // 연관 사용자 설정
        
        Board savedBoard = boardRepository.save(board);
        
        // Elasticsearch 저장
        BoardES boardES = new BoardES();
        boardES.setId(savedBoard.getId()); // Board의 ID를 문자열로 변환하여 설정
        boardES.setTitle(savedBoard.getTitle());
        boardES.setContent(savedBoard.getContent());
        boardES.setUsername(username);
        boardES.setCreateDate(Timestamp.from(savedBoard.getCreateDate().toInstant()));
        boardESRepository.save(boardES);
        
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
		
		// 댓글
		List<Reply> replies = replyRepository.findByBoardid(board.getId());
		
		// 대댓글
		List<Long> replyIds = replies.stream().map(Reply::getId).collect(Collectors.toList());
		List<ReReply> reReplies = reReplyRepositoy.findByReplyidIn(replyIds);
		
		// 댓, 대댓 DTO 변환
		List<ReplyDetailDto> replyDtos = replies.stream()
	            .map(reply -> new ReplyDetailDto(reply.getId(), reply.getContent(), reply.getUsername(), reply.getCreateDate()))
	            .collect(Collectors.toList());

	    List<ReReplyDetailDto> reReplyDtos = reReplies.stream()
	            .map(reReply -> new ReReplyDetailDto(reReply.getId(), reReply.getContent(), reReply.getUsername(), reReply.getCreateDate(), reReply.getReplyid()))
	            .collect(Collectors.toList());
		
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
				isLiked,
				replyDtos,
				reReplyDtos
		);
	}
	
	@Transactional
	public void 글수정(Long id, BoardWriteDto boardWriteDto, String username) {
		
		Board board = boardRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
		
		if (!board.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("본인의 글만 수정할 수 있습니다.");
        }
		
		// DB
		board.setTitle(boardWriteDto.getTitle());
		board.setContent(boardWriteDto.getContent());
		boardRepository.save(board);
		
		// Elasticsearch
	    Optional<BoardES> boardESOptional = boardESRepository.findById(id);
	    if (boardESOptional.isPresent()) {
	        BoardES boardES = boardESOptional.get();
	        boardES.setTitle(boardWriteDto.getTitle());
	        boardES.setContent(boardWriteDto.getContent());
	        boardESRepository.save(boardES);
	    } else {
	    	System.out.println("Elasticsearch에 해당 ID의 게시글이 없음.");
	    }
		
	}
	
	@Transactional
	public void 글삭제(Long id, String username) {
		 
		Board board = boardRepository.findById(id)
	                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

		if (!board.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("본인의 글만 삭제할 수 있습니다.");
        }
		
	    boardCountRepository.deleteById(id); // 조회수 엔티티 삭제
	    boardLikeTotalRepository.deleteById(id); // 전체 추천수 엔티티 삭제
	    boardLikeRepository.deleteById(id); // 추천 user 삭제
	    
	    // Elasticsearch
	    boardESRepository.deleteById(id);
	    
	    // redis
		redisUtil.deleteBoardView(id);
		redisUtil.deleteBoardLike(id);
		redisUtil.deleteBoardLikeTotal(id);
		redisUtil.deletecanIncrementView(username, id);
		
		// 댓글 조회
		List<Reply> replies = replyRepository.findByBoardid(id);
		
		// 대댓글 조회
		List<Long> replyIds = replies.stream().map(Reply::getId).collect(Collectors.toList());
		
		// 대댓글 먼저 삭제
		if (!replyIds.isEmpty()) {
	        reReplyRepositoy.deleteByReplyidIn(replyIds);
	    }
		
		// 댓글
		replyRepository.deleteByBoardid(id);
		
		// DB
		boardRepository.delete(board);
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
	
	@Transactional(readOnly = true)
	public Page<BoardFindDto> 제목찾기(String title, Pageable pageable) {
	    Page<BoardES> boardESPage = boardESRepository.findByTitleContaining(title, pageable);
	    
	    return boardESPage.map(boardES -> {
	        Long boardId = Long.parseLong(boardES.getId().toString());
	        int viewCount = redisUtil.getBoardView(boardId);
	        int likeCount = redisUtil.getBoardLikeTotal(boardId);
	        
	        return new BoardFindDto(
	            boardId,
	            boardES.getTitle(),
	            boardES.getUsername(),
	            boardES.getCreateDate().toInstant(),
	            viewCount,
	            likeCount
	        );
	    });
	}

	@Transactional(readOnly = true)
	public Page<BoardFindDto> 내용찾기(String content, Pageable pageable) {
	    Page<BoardES> boardESPage = boardESRepository.findByContentContaining(content, pageable);
	    
	    return boardESPage.map(boardES -> {
	        Long boardId = Long.parseLong(boardES.getId().toString());
	        int viewCount = redisUtil.getBoardView(boardId);
	        int likeCount = redisUtil.getBoardLikeTotal(boardId);
	        
	        return new BoardFindDto(
	            boardId,
	            boardES.getTitle(),
	            boardES.getUsername(),
	            boardES.getCreateDate().toInstant(),
	            viewCount,
	            likeCount
	        );
	    });
	}

	@Transactional(readOnly = true)
	public Page<BoardFindDto> 제목내용찾기(String keyword, Pageable pageable) {
	    Page<BoardES> boardESPage = boardESRepository.findByTitleContainingOrContentContaining(keyword, keyword, pageable);
	    
	    return boardESPage.map(boardES -> {
	        Long boardId = Long.parseLong(boardES.getId().toString());
	        int viewCount = redisUtil.getBoardView(boardId);
	        int likeCount = redisUtil.getBoardLikeTotal(boardId);
	        
	        return new BoardFindDto(
	            boardId,
	            boardES.getTitle(),
	            boardES.getUsername(),
	            boardES.getCreateDate().toInstant(),
	            viewCount,
	            likeCount
	        );
	    });
	}
}
