package com.sang.health.repository.board;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sang.health.entity.board.BoardLike;

public interface BoardLikeRepository extends JpaRepository<BoardLike, Long>{

}
