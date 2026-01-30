package com.shcho.myBlog.category.dto;

import com.shcho.myBlog.category.entity.Category;

import java.util.List;

public record CategoryTreeResponseDto(
        Long categoryId,
        String name,
        String description,
        List<CategoryChildDto> children
) {
    public static CategoryTreeResponseDto of(Category root, List<CategoryChildDto> children) {
        return new CategoryTreeResponseDto(
                root.getId(),
                root.getName(),
                root.getDescription(),
                children
        );
    }

    public static CategoryChildDto childOf(Category child) {
        return new CategoryChildDto(
                child.getId(),
                child.getName(),
                child.getDescription()
        );
    }

    public record CategoryChildDto(
            Long id,
            String name,
            String description
    ) {
    }
}
