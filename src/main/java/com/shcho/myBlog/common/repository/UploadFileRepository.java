package com.shcho.myBlog.common.repository;

import com.shcho.myBlog.common.entity.UploadFile;
import com.shcho.myBlog.common.entity.UploadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UploadFileRepository extends JpaRepository<UploadFile, Long> {
    List<UploadFile> findAllByPostIdAndStatus(Long postId, UploadStatus status);
}
