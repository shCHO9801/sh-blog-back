package com.shcho.myBlog.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserSignUpRequestDto(
        @NotBlank(message = "아이디는 필수 입니다.") String username,
        @NotBlank(message = "비밀번호는 필수 입니다.") String password,
        @NotBlank(message = "닉네임은 필수 입니다.") String nickname,
        @NotBlank(message = "이메일은 필수 입니다.") @Email String email
) {
}
