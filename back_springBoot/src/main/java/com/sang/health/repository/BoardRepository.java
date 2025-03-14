package com.sang.health.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.sang.health.entity.Board;

public interface BoardRepository extends JpaRepository<Board, Long>{
	
	@EntityGraph(attributePaths = {"user"})
	Page<Board> findAll(Pageable pageable);
}
