package com.shcho.myBlog.category.dto;

public record DeleteCategoryResponseDto(
        Long categoryId,
        String message
) {
    private final static String DELETE_MESSAGE = "카테고리 삭제에 성공했습니다.";

    public static DeleteCategoryResponseDto of(Long categoryId) {
        return new DeleteCategoryResponseDto(
                categoryId,
                DELETE_MESSAGE
        );
    }
}
