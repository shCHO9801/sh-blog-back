package com.shcho.myBlog.post.dto;

public record DeletePostResponseDto(
        Long deletedPostId,
        String message
) {
    private final static String DELETE_MESSAGE = "게시글 삭제에 성공했습니다.";

    public static DeletePostResponseDto from(Long deletedPostId) {
        return new DeletePostResponseDto(deletedPostId, DELETE_MESSAGE);
    }
}
