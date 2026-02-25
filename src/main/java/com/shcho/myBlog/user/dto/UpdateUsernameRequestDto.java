package com.shcho.myBlog.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUsernameRequestDto(
        @NotBlank String username
) {
}
