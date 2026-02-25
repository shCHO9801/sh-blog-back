package com.shcho.myBlog.user.dto;

import com.shcho.myBlog.user.entity.User;

public record UserSignInResponseDto(
        String accessToken,
        String username,
        String nickname,
        String email,
        long expiresIn
) {
    public static UserSignInResponseDto of(User user, String token, long expiresIn) {
        return new UserSignInResponseDto(
                token,
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                expiresIn
        );
    }
}
