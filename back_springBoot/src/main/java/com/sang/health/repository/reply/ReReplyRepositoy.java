package com.sang.health.repository.reply;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sang.health.entity.reply.ReReply;

public interface ReReplyRepositoy extends JpaRepository<ReReply, Long>{

	List<ReReply> findByReplyidIn(List<Long> replyIds); // In 연산자 사용
	
	List<ReReply> deleteByReplyidIn(List<Long> replyIds);
}
