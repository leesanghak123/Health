package com.sang.health.batch;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
public class scheduling {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    public scheduling(JobLauncher jobLauncher, JobRegistry jobRegistry) {
        this.jobLauncher = jobLauncher;
        this.jobRegistry = jobRegistry;
    }

    // ES 배치 작업 - 매일 자정
    @Scheduled(cron = "0 12 13 * * *", zone = "Asia/Seoul")
    public void runESJob() throws Exception {
        System.out.println("ES batch job starting at " + new Date());
        
        JobParameters jobParameters = createJobParameters("ES-job");
        jobLauncher.run(jobRegistry.getJob("ESJob"), jobParameters);
    }
    
    // Redis 배치 작업 - 3분 마다
    @Scheduled(fixedRate = 180000, zone = "Asia/Seoul")
    public void runLikeCheckJob() throws Exception {
        System.out.println("Redis batch job starting at " + new Date());
        
        JobParameters jobParameters = createJobParameters("Redis-like-check-job");
        jobLauncher.run(jobRegistry.getJob("LikeCheckJob"), jobParameters);
    }
    
    // Redis 배치 작업 - 3분 마다
    @Scheduled(fixedRate = 180000, zone = "Asia/Seoul")
    public void runLikeCountJob() throws Exception {
        System.out.println("Redis batch job starting at " + new Date());
        
        JobParameters jobParameters = createJobParameters("Redis-like-count-job");
        jobLauncher.run(jobRegistry.getJob("LikeCountJob"), jobParameters);
    }
    
    // Redis 배치 작업 - 3분 마다
    @Scheduled(fixedRate = 180000, zone = "Asia/Seoul")
    public void runViewJob() throws Exception {
        System.out.println("Redis batch job starting at " + new Date());
        
        JobParameters jobParameters = createJobParameters("Redis-view-job");
        jobLauncher.run(jobRegistry.getJob("ViewJob"), jobParameters);
    }
    
    // 파라미터 생성 메서드
    private JobParameters createJobParameters(String prefix) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String date = dateFormat.format(new Date());
        
        return new JobParametersBuilder()
            .addString("date", date)
            .addString("jobType", prefix)
            .toJobParameters();
    }
}