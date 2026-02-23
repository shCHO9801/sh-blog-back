package com.shcho.myBlog.post.dto;

import java.time.LocalDateTime;

public record PostRecentThumbnailResponseDto(
        Long postId,
        String title,
        String summary,
        String categoryName,
        LocalDateTime createdAt
) {
}
