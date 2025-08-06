package com.sang.health.service.reply;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sang.health.dto.reply.ReReplyWriteDto;
import com.sang.health.dto.reply.ReplyWriteDto;
import com.sang.health.entity.reply.ReReply;
import com.sang.health.entity.reply.Reply;
import com.sang.health.repository.board.BoardRepository;
import com.sang.health.repository.reply.ReReplyRepositoy;
import com.sang.health.repository.reply.ReplyRepository;
import com.sang.health.repository.user.UserRepository;
import com.sang.health.util.HtmlSanitizerUtil;

@Service
public class ReplyService {

	private final UserRepository userRepository;
	private final BoardRepository boardRepository;
	private final ReplyRepository replyRepository;
	private final ReReplyRepositoy reReplyRepositoy;
	
	public ReplyService(BoardRepository boardRepository, ReplyRepository replyRepository, ReReplyRepositoy reReplyRepositoy, UserRepository userRepository) {
		this.boardRepository = boardRepository;
		this.replyRepository = replyRepository;
		this.reReplyRepositoy = reReplyRepositoy;
		this.userRepository = userRepository;
	}
	
	@Transactional
	public void 댓글작성(ReplyWriteDto replyWriteDto, String username) {
		
		boardRepository.findById(replyWriteDto.getBoardid())
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
		
		userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
		
		// Sanitizer로 정제
		String sanitizedContent = HtmlSanitizerUtil.sanitize(replyWriteDto.getContent());
		
		Reply reply = new Reply();
	    reply.setBoardid(replyWriteDto.getBoardid());
	    reply.setUsername(username);
	    reply.setContent(sanitizedContent);
	    
	    replyRepository.save(reply);
	}
	
	@Transactional
	public void 대댓글작성(ReReplyWriteDto rereplyWriteDto, String username) {
		
		replyRepository.findById(rereplyWriteDto.getReplyid())
        .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));

		userRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
		
		// Sanitizer로 정제
		String sanitizedContent = HtmlSanitizerUtil.sanitize(rereplyWriteDto.getContent());
		
		ReReply rereply = new ReReply();
		rereply.setReplyid(rereplyWriteDto.getReplyid());
		rereply.setUsername(username);
		rereply.setContent(sanitizedContent);
		
		reReplyRepositoy.save(rereply);
	}
	
	@Transactional
	public void 댓글삭제(Long id, String username) {

		Reply reply = replyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 댓글이 존재하지 않습니다."));
		
		if (!reply.getUsername().equals(username)) {
			throw new IllegalArgumentException("본인의 댓글만 삭제할 수 있습니다.");
		}
		
		replyRepository.delete(reply);
	}
	
	@Transactional
	public void 대댓글삭제(Long id, String username) {

		ReReply reReply = reReplyRepositoy.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 대댓글이 존재하지 않습니다."));
		
		if (!reReply.getUsername().equals(username)) {
			throw new IllegalArgumentException("본인의 대댓글만 삭제할 수 있습니다.");
		}
		
		reReplyRepositoy.delete(reReply);
	}
}
