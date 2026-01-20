package com.shcho.myBlog.user.dto;

import com.shcho.myBlog.user.entity.User;

public record UserMeResponseDto(
        String username,
        String nickname,
        String email
) {
    public static UserMeResponseDto from(User user) {
        return new UserMeResponseDto(
                user.getUsername(),
                user.getNickname(),
                user.getEmail()
        );
    }
}
