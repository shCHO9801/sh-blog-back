package com.shcho.myBlog.user.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record UpdateUserProfileImageRequestDto(
        @NotBlank @URL String profileImageUrl
) {
}
