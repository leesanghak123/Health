package com.sang.health.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sang.health.entity.BoardCount;

public interface BoardCountRepository extends JpaRepository<BoardCount, Long>{

}
