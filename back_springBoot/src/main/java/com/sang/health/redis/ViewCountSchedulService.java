package com.sang.health.redis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j // log
@Service
public class ViewCountSchedulService {

	private final RedisUtil redisUtil;
    private final JdbcTemplate jdbcTemplate;

    public ViewCountSchedulService(RedisUtil redisUtil, JdbcTemplate jdbcTemplate) {
        this.redisUtil = redisUtil;
        this.jdbcTemplate = jdbcTemplate;
    }
    
	@Scheduled(fixedRate = 60000) // 1분
	@Transactional
	public void ViewCountToDB() {
		
		Set<String> keys = redisUtil.getKeysWithPattern("board:view:*");
		
		if (keys.isEmpty()) {
            return;
        }
		
		// 배치 처리를 위한 리스트
        List<Object[]> batchArgs = new ArrayList<>();
		
		for (String key : keys) {
            try {
                // boardId 파싱
                String boardIdStr = key.substring("board:view:".length());
                Long boardId = Long.parseLong(boardIdStr);
               
                // Redis에서 조회수 가져오기
                int viewCount = redisUtil.getBoardView(boardId);
                
                batchArgs.add(new Object[] {viewCount, boardId});
                
            } catch (Exception e) {
                log.error("조회수 동기화 중 오류 발생: " + key, e);
            }
        }
		// 배치로 한 번에 저장
		if (!batchArgs.isEmpty()) {
			int[] updateCounts = this.jdbcTemplate.batchUpdate(
		            "UPDATE boardCount SET count = ? WHERE boardId = ?",
		            batchArgs
		        );
		        
		        log.info("업데이트된 게시판 수: " + Arrays.stream(updateCounts).sum());
        }
	}	
}
