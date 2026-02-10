package com.shcho.myBlog.common.dto;

public record FileUploadResponseDto(
        String url
) {
    public static FileUploadResponseDto from(String url) {
        return new FileUploadResponseDto(url);
    }
}
