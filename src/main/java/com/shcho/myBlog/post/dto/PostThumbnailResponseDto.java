package com.shcho.myBlog.post.dto;

import java.time.LocalDateTime;

public record PostThumbnailResponseDto(
        Long postId,
        String categoryName,
        String title,
        boolean isPublic,
        LocalDateTime createdAt
) {
}
