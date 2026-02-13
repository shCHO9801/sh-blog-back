package com.shcho.myBlog.common.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UploadFileCleanupJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job uploadFileCleanupJob;

    @Scheduled(cron = "${cleanup.schedule-cron:0 0 30 * * *}")
    public void runUploadFileCleanupJob() {
        Long runAt = System.currentTimeMillis();
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("runAt", runAt)
                    .toJobParameters();

            jobLauncher.run(uploadFileCleanupJob, params);
            log.info("uploadFileCleanupJob 실행 완료. runAt = {}", runAt);
        } catch (Exception e) {
            log.error("uploadFileCleanupJob 실행 실패", e);
        }
    }
}
