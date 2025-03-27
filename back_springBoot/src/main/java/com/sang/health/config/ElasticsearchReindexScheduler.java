//package com.sang.health.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//
//import com.sang.health.service.board.ElasticsearchIndexManageService;
//
//@Configuration
//@EnableScheduling
//public class ElasticsearchReindexScheduler {
//    
//	private ElasticsearchIndexManageService indexManageService;
//    
//    public ElasticsearchReindexScheduler(ElasticsearchIndexManageService indexManageService) {
//    	this.indexManageService = indexManageService;
//    }
//
//    // 매일 자정에 재색인
//    @Scheduled(cron = "0 0 0 * * ?")
//    public void scheduleReindex() {
//        indexManageService.createNewIndexAndReindex();
//    }
//}