package com.shcho.myBlog.category.controller;

import com.shcho.myBlog.category.dto.CreateCategoryRequestDto;
import com.shcho.myBlog.category.dto.CreateCategoryResponseDto;
import com.shcho.myBlog.category.entity.Category;
import com.shcho.myBlog.category.service.CategoryService;
import com.shcho.myBlog.user.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CreateCategoryResponseDto> createMyCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateCategoryRequestDto requestDto
    ) {
        Category newCategory = categoryService.createMyCategory(userDetails.getUserId(), requestDto);

        return ResponseEntity.ok(CreateCategoryResponseDto.from(newCategory));
    }


}
