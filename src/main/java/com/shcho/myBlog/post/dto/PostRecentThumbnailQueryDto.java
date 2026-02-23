package com.shcho.myBlog.post.dto;

import java.time.LocalDateTime;

public record PostRecentThumbnailQueryDto(
        Long postId,
        String title,
        String content,
        String categoryName,
        LocalDateTime createdAt
) {
}
