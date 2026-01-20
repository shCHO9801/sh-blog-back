package com.shcho.myBlog.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserSignInRequestDto(
        @NotBlank(message = "아이디는 필수 입니다.") String username,
        @NotBlank(message = "비밀번호는 필수 입니다.") String password
) {
}
