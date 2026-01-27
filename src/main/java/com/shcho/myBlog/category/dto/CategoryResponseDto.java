package com.shcho.myBlog.category.dto;

import com.shcho.myBlog.category.entity.Category;

public record CategoryResponseDto(
        Long categoryId,
        String name,
        String description,
        boolean root
) {
    public static CategoryResponseDto of(Category category) {
        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.isRoot()
        );
    }
}
