package com.shcho.myBlog.common.service;

import com.shcho.myBlog.common.entity.UploadFile;
import com.shcho.myBlog.common.entity.UploadStatus;
import com.shcho.myBlog.common.entity.UploadType;
import com.shcho.myBlog.common.repository.UploadFileRepository;
import com.shcho.myBlog.libs.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.shcho.myBlog.libs.exception.ErrorCode.FILE_FORBIDDEN;
import static com.shcho.myBlog.libs.exception.ErrorCode.FILE_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class UploadFileService {

    private final UploadFileRepository uploadFileRepository;

    @Transactional
    public Long saveTemp(Long userId, UploadType type, String objectName, String url) {
        UploadFile uploadFile = UploadFile.builder()
                .userId(userId)
                .postId(null)
                .type(type)
                .objectName(objectName)
                .url(url)
                .status(UploadStatus.TEMP)
                .build();

        return uploadFileRepository.save(uploadFile).getId();
    }

    @Transactional
    public void attachFilesToPost(Long userId, Long postId, String content) {
        Set<Long> fids = extractFids(content);
        if (fids.isEmpty()) {
            return;
        }

        List<UploadFile> toUpload = new ArrayList<>(fids.size());

        for (Long fid : fids) {
            UploadFile file = uploadFileRepository.findById(fid)
                    .orElseThrow(() -> new CustomException(FILE_NOT_FOUND));

            if (file.getStatus() == UploadStatus.DELETED) {
                throw new CustomException(FILE_NOT_FOUND);
            }

            if (!Objects.equals(file.getUserId(), userId)) {
                throw new CustomException(FILE_FORBIDDEN);
            }

            file.attachToPost(postId);
            toUpload.add(file);
        }

        uploadFileRepository.saveAll(toUpload);
    }

    public Set<Long> extractFids(String content) {
        if (content == null || content.isBlank()) {
            return Collections.emptySet();
        }

        // (?!) 대소문자 무시
        Pattern pattern = Pattern.compile("(?i)[?&]fid=(\\d+)");
        Matcher matcher = pattern.matcher(content);

        Set<Long> result = new LinkedHashSet<>();
        while (matcher.find()) {
            String raw = matcher.group(1);
            try {
                result.add(Long.parseLong(raw));
            } catch (NumberFormatException ignored) {

            }
        }
        return result;
    }

    public List<UploadFile> getAttachedFilesByPostId(Long postId) {
        return uploadFileRepository.findAllByPostIdAndStatus(postId, UploadStatus.ATTACHED);
    }

    @Transactional
    public void markDeletedAndSaveAll(List<UploadFile> files) {
        if (files == null || files.isEmpty()) return;

        for (UploadFile file : files) {
            file.markDeleted();
        }
        uploadFileRepository.saveAll(files);
    }

    public String appendFid(String url, Long fileId) {
        return url.contains("?") ? url + "&fid=" + fileId : url + "?fid=" + fileId;
    }
}
