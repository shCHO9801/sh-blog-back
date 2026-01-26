package com.shcho.myBlog.category.dto;

import com.shcho.myBlog.category.entity.Category;

public record CreateCategoryResponseDto(
        Long blogId,
        Long categoryId,
        String name,
        String description
) {

    public static CreateCategoryResponseDto from(Category category) {
        return new CreateCategoryResponseDto(
                category.getBlog().getId(),
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}
