package com.shcho.myBlog.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserNicknameRequestDto(
        @NotBlank String nickname
) {
}
