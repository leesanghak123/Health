package com.sang.health.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sang.health.entity.Board;

public interface BoardRepository extends JpaRepository<Board, Long>{

}
