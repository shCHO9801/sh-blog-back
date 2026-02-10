package com.shcho.myBlog.common.service;

import com.shcho.myBlog.common.entity.UploadType;
import com.shcho.myBlog.libs.exception.CustomException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static com.shcho.myBlog.common.entity.UploadType.ATTACHMENT;
import static com.shcho.myBlog.common.entity.UploadType.IMAGE;
import static com.shcho.myBlog.libs.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.base-url}")
    private String minioBaseUrl;

    private static final long IMAGE_MAX_SIZE = 10L * 1024 * 1024;
    private static final long ATTACH_MAX_SIZE = 50L * 1024 * 1024;

    private static final Set<String> IMAGE_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ATTACH_EXT = Set.of("pdf", "zip", "txt", "md");

    public String upload(Long userId, MultipartFile file, UploadType type) {
        validateFile(file, type);

        String ext = extractExt(file.getOriginalFilename());
        String objectName = buildObjectName(userId, type, ext);

        try (InputStream is = file.getInputStream()) {

            String contentType = file.getContentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(is, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );

            return buildFileUrl(objectName);
        } catch (Exception e) {
            log.error("MinIO 업로드 실패. userId = {}, type = {}, objectName = {}, reason = {}",
                    userId, type, objectName, e.getMessage(), e);
            throw new CustomException(FILE_UPLOAD_FAILED);
        }
    }

    private void validateFile(MultipartFile file, UploadType type) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(FILE_EMPTY);
        }

        long size = file.getSize();

        if (type == IMAGE && size > IMAGE_MAX_SIZE) {
            throw new CustomException(FILE_TOO_LARGE);
        }
        if (type == ATTACHMENT && size > ATTACH_MAX_SIZE) {
            throw new CustomException(FILE_TOO_LARGE);
        }

        String ext = extractExt(file.getOriginalFilename());

        if (type == IMAGE && !IMAGE_EXT.contains(ext)) {
            throw new CustomException(INVALID_FILE_EXTENSION);
        }
        if (type == ATTACHMENT && !ATTACH_EXT.contains(ext)) {
            throw new CustomException(INVALID_FILE_EXTENSION);
        }
    }

    private String extractExt(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new CustomException(INVALID_FILE_EXTENSION);
        }
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        if (ext.isBlank()) {
            throw new CustomException(INVALID_FILE_EXTENSION);
        }
        return ext;
    }

    private String buildObjectName(Long userId, UploadType type, String ext) {
        LocalDate now = LocalDate.now();
        String yyyy = String.valueOf(now.getYear());
        String mm = String.format("%02d", now.getMonthValue());

        String folder = (type == IMAGE) ? "images" : "attachments";
        String uuid = UUID.randomUUID().toString().replace("-", "");

        return "users/" + userId + "/" + folder + "/" + yyyy + "/" + mm + "/" + uuid + "." + ext;
    }

    private String buildFileUrl(String objectName) {
        String encoded = encodeObjectPath(objectName);
        String base = minioBaseUrl.endsWith("/") ? minioBaseUrl.substring(0, minioBaseUrl.length() - 1) : minioBaseUrl;

        return base + "/" + bucket + "/" + encoded;
    }

    private String encodeObjectPath(String objectName) {
        String[] parts = objectName.split("/");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append("/");
            sb.append(URLEncoder.encode(parts[i], StandardCharsets.UTF_8));
        }
        return sb.toString();
    }
}
