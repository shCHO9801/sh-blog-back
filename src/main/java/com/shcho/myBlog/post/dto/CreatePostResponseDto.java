package com.shcho.myBlog.post.dto;

import com.shcho.myBlog.post.entity.Post;

import java.time.LocalDateTime;

public record CreatePostResponseDto(
        Long postId,
        Long blogId,
        Long categoryId,
        String title,
        String content,
        boolean isPublic,
        LocalDateTime createdAt
) {
    public static CreatePostResponseDto from(Post post) {
        return new CreatePostResponseDto(
                post.getId(),
                post.getBlog().getId(),
                post.getCategory().getId(),
                post.getTitle(),
                post.getContent(),
                post.isPublic(),
                post.getCreatedAt()
        );
    }
}
