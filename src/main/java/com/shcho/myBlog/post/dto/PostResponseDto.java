package com.shcho.myBlog.post.dto;

import com.shcho.myBlog.post.entity.Post;

import java.time.LocalDateTime;

public record PostResponseDto(
        Long postId,
        Long categoryId,
        String title,
        String content,
        boolean isPublic,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostResponseDto from(Post post) {
        return new PostResponseDto(
                post.getId(),
                post.getCategory().getId(),
                post.getTitle(),
                post.getContent(),
                post.isPublic(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
