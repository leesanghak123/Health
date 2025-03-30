package com.sang.health.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.sang.health.service.board.BoardIndexService;

@Configuration
@EnableScheduling
public class ElasticsearchSchedulerConfig {

    private final BoardIndexService boardIndexService;
    
    public ElasticsearchSchedulerConfig(BoardIndexService boardIndexService) {
    	this.boardIndexService = boardIndexService;
    }

    // 매일 자정에 전체 재색인
    @Scheduled(cron = "0 0 0 * * ?")
    public void performFullReindexing() {
        boardIndexService.reindexAllBoards();
    }
}