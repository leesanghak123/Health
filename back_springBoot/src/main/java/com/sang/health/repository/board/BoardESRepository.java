package com.sang.health.repository.board;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.sang.health.entity.board.BoardES;

public interface BoardESRepository extends ElasticsearchRepository<BoardES, Long>{

	// 제목으로 검색
    Page<BoardES> findByTitleContaining(String title, Pageable pageable);
    
    // 내용으로 검색
    Page<BoardES> findByContentContaining(String content, Pageable pageable);
    
    // 제목이나 내용에 키워드가 포함된 게시글 검색
    Page<BoardES> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
}
