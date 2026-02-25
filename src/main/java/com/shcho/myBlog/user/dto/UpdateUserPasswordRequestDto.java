package com.shcho.myBlog.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserPasswordRequestDto(
        @NotBlank String oldPassword,
        @NotBlank String rawPassword
) {
}
