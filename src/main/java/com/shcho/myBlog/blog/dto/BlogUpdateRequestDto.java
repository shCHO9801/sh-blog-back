package com.shcho.myBlog.blog.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BlogUpdateRequestDto(
        @Size(max = 100)
        @Pattern(regexp = ".*\\S.*", message = "title은 공백만 올 수 없습니다.")
        String title,
        @Size(max = 500) String intro,
        String bannerImageUrl
) {
}
