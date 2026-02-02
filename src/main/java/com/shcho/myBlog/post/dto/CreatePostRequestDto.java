package com.shcho.myBlog.post.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePostRequestDto (
        @NotBlank String title,
        String content,
        Long categoryId
){
}
