package com.sang.health.repository.board;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sang.health.entity.board.Board;

public interface BoardRepository extends JpaRepository<Board, Long>{

	// Elastic 배치 시 사용
	@EntityGraph(attributePaths = "user")  // user를 즉시 로딩
    Page<Board> findAll(Pageable pageable);
}
