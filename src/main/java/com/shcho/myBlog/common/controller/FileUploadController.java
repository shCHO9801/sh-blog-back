package com.shcho.myBlog.common.controller;

import com.shcho.myBlog.common.dto.FileUploadResponseDto;
import com.shcho.myBlog.common.service.MinioService;
import com.shcho.myBlog.user.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static com.shcho.myBlog.common.entity.UploadType.ATTACHMENT;
import static com.shcho.myBlog.common.entity.UploadType.IMAGE;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final MinioService minioService;

    @PostMapping("/upload/images")
    public ResponseEntity<FileUploadResponseDto> uploadImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file
    ) {
        String url = minioService.upload(userDetails.getUserId(), file, IMAGE);
        return ResponseEntity.ok(FileUploadResponseDto.from(url));
    }

    @PostMapping("/upload/attachments")
    public ResponseEntity<FileUploadResponseDto> uploadFile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile file
    ) {
        String url = minioService.upload(userDetails.getUserId(), file, ATTACHMENT);
        return ResponseEntity.ok(FileUploadResponseDto.from(url));
    }
}
