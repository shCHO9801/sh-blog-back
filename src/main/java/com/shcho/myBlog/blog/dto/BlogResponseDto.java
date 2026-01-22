package com.shcho.myBlog.blog.dto;

import com.shcho.myBlog.blog.entity.Blog;

public record BlogResponseDto(
        Long id,
        String title,
        String intro,
        String bannerImageUrl,
        String userNickName,
        String userProfileImageUrl
) {
    public static BlogResponseDto from(Blog myBlog) {
        return new BlogResponseDto(
                myBlog.getId(),
                myBlog.getTitle(),
                myBlog.getIntro(),
                myBlog.getBannerImageUrl(),
                myBlog.getUser().getNickname(),
                myBlog.getUser().getProfileImageUrl()
        );
    }
}
