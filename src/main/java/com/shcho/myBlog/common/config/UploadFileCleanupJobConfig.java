package com.shcho.myBlog.common.config;

import com.shcho.myBlog.common.entity.UploadFile;
import com.shcho.myBlog.common.entity.UploadStatus;
import com.shcho.myBlog.common.repository.UploadFileRepository;
import com.shcho.myBlog.common.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class UploadFileCleanupJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final UploadFileRepository uploadFileRepository;
    private final MinioService minioService;

    @Value("${cleanup.temp-ttl-hours:24}")
    private Long tempTtlHours;

    @Value("${cleanup.deleted-retention-days:7}")
    private Long deletedRetentionDays;

    @Bean
    public Job uploadFileCleanupJob(
            @Qualifier("cleanupTempFilesStep") Step cleanupTempFilesStep,
            @Qualifier("purgeDeletedFilesStep") Step purgeDeletedFilesStep
    ) {
        return new JobBuilder("uploadFileCleanupJob", jobRepository)
                .start(cleanupTempFilesStep)
                .next(purgeDeletedFilesStep)
                .build();
    }

    @Bean("cleanupTempFilesStep")
    public Step cleanupTempFilesStep(
            @Qualifier("tempFilesReader") RepositoryItemReader<UploadFile> tempFilesReader
    ) {
        return new StepBuilder("cleanupTempFilesStep", jobRepository)
                .<UploadFile, UploadFile>chunk(100, transactionManager)
                .reader(tempFilesReader)
                .writer(tempFilesWriter())
                .build();
    }

    @Bean("purgeDeletedFilesStep")
    public Step purgeDeletedFilesStep(
            @Qualifier("deletedFilesReader") RepositoryItemReader<UploadFile> deletedFilesReader
    ) {
        return new StepBuilder("purgeDeletedFilesStep", jobRepository)
                .<UploadFile, UploadFile>chunk(100, transactionManager)
                .reader(deletedFilesReader)
                .writer(deletedFilesWriter())
                .build();
    }

    @Bean("tempFilesReader")
    @StepScope
    public RepositoryItemReader<UploadFile> tempFilesReader(
            @Value("#{jobParameters['runAt']}") Long runAt
    ) {
        LocalDateTime cutoff = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(runAt),
                ZoneId.of("Asia/Seoul")
        ).minusHours(tempTtlHours);

        return new RepositoryItemReaderBuilder<UploadFile>()
                .name("tempFilesReader")
                .repository(uploadFileRepository)
                .methodName("findAllByStatusAndCreatedAtBefore")
                .arguments(UploadStatus.TEMP, cutoff)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .pageSize(100)
                .build();
    }

    @Bean("deletedFilesReader")
    @StepScope
    public RepositoryItemReader<UploadFile> deletedFilesReader(
            @Value("#{jobParameters['runAt']}") Long runAt
    ) {
        LocalDateTime cutoff = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(runAt),
                ZoneId.of("Asia/Seoul")
        ).minusDays(deletedRetentionDays);

        return new RepositoryItemReaderBuilder<UploadFile>()
                .name("deletedFilesReader")
                .repository(uploadFileRepository)
                .methodName("findAllByStatusAndDeletedAtBefore")
                .arguments(UploadStatus.DELETED, cutoff)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .pageSize(100)
                .build();
    }

    @Bean
    public ItemWriter<UploadFile> tempFilesWriter() {
        return items -> {
            var list = items.getItems();

            for (UploadFile file : list) {
                try {
                    minioService.deleteObject(file.getObjectName());
                    file.markDeleted();
                } catch (Exception e) {
                    log.warn("TEMP 파일 MinIO 삭제 실패. uploadFileId={}, objectName={}",
                            file.getId(), file.getObjectName(), e);
                }
            }

            uploadFileRepository.saveAll(list);
        };
    }

    @Bean
    public ItemWriter<UploadFile> deletedFilesWriter() {
        return items -> {
            var list = items.getItems();
            var successIds = new ArrayList<Long>();

            for (UploadFile file : list) {
                try {
                    minioService.deleteObject(file.getObjectName());
                    successIds.add(file.getId());
                } catch (Exception e) {
                    log.warn("DELETED 파일 MinIO 삭제 실패. uploadFileId={}, objectName={}",
                            file.getId(), file.getObjectName(), e);
                }
            }

            if (!successIds.isEmpty()) {
                uploadFileRepository.deleteAllByIdInBatch(successIds);
            }
        };
    }
}
