package com.shcho.myBlog.user.dto;

import com.shcho.myBlog.user.entity.User;

import java.time.LocalDateTime;

public record UserSignUpResponseDto(
        Long userId,
        String username,
        String nickname,
        String email,
        LocalDateTime createdAt
) {
    public static UserSignUpResponseDto from(User user) {
        return new UserSignUpResponseDto(
                user.getUserId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
