package com.shcho.myBlog.common.dto;


import org.springframework.data.domain.Page;

import java.util.List;

public record PagedResponseDto<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static <T> PagedResponseDto<T> from(Page<T> page) {
        return new PagedResponseDto<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
