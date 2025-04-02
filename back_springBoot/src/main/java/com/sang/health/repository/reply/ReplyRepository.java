package com.sang.health.repository.reply;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sang.health.entity.reply.Reply;

public interface ReplyRepository extends JpaRepository<Reply, Long>{

	List<Reply> findByBoardid(Long boardId);
	
	List<Reply> deleteByBoardid(Long boardId);
}
