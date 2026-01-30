package com.shcho.myBlog.category.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCategoryRequest(
        @NotBlank String name,
        String description,
        Long parentId
) {
}
