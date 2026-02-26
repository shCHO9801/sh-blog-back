package com.shcho.myBlog.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserEmailRequestDto(
        @NotBlank @Email String email
) {
}
