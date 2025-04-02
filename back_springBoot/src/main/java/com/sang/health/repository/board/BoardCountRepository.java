package com.sang.health.repository.board;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sang.health.entity.board.BoardCount;

public interface BoardCountRepository extends JpaRepository<BoardCount, Long>{

}
