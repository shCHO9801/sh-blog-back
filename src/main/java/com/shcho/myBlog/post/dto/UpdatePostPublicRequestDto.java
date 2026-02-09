package com.shcho.myBlog.post.dto;

import jakarta.validation.constraints.NotNull;

public record UpdatePostPublicRequestDto(
        @NotNull Boolean isPublic
) {
}
