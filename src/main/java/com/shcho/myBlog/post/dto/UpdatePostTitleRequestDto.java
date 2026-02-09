package com.shcho.myBlog.post.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePostTitleRequestDto(
        @NotBlank String title
) {
}
