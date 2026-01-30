package com.shcho.myBlog.category.controller;

import com.shcho.myBlog.category.dto.*;
import com.shcho.myBlog.category.entity.Category;
import com.shcho.myBlog.category.service.CategoryService;
import com.shcho.myBlog.user.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/my")
    public ResponseEntity<List<CategoryTreeResponseDto>> getMyCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<CategoryTreeResponseDto> myCategoryTree =
                categoryService.getMyCategoryTree(userDetails.getUserId());

        return ResponseEntity.ok(myCategoryTree);
    }

    @GetMapping("/my/roots")
    public ResponseEntity<List<CategoryResponseDto>> getMyRootCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<Category> myRootCategories = categoryService.getMyRootCategories(userDetails.getUserId());
        List<CategoryResponseDto> rootCategoryList = myRootCategories.stream()
                .map(CategoryResponseDto::of)
                .toList();

        return ResponseEntity.ok(rootCategoryList);
    }

    @GetMapping("/my/{categoryId}")
    public ResponseEntity<CategoryResponseDto> getMyCategoryByCategoryId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long categoryId
    ) {
        Category category = categoryService.getMyCategoryByCategoryId(userDetails.getUserId(), categoryId);
        return ResponseEntity.ok(CategoryResponseDto.of(category));
    }

    @GetMapping("/public/{nickname}")
    public ResponseEntity<List<CategoryTreeResponseDto>> getCategoryByBlogId(
            @PathVariable String nickname
    ) {
        List<CategoryTreeResponseDto> getBlogCategory =
                categoryService.getCategoryTreeByNickname(nickname);

        return ResponseEntity.ok(getBlogCategory);
    }

    @PatchMapping("/my/{categoryId}")
    public ResponseEntity<CategoryResponseDto> updateMyCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long categoryId,
            @RequestBody @Valid UpdateCategoryRequest request
    ) {
        Category updatedCategory = categoryService.updateCategory(userDetails.getUserId(), categoryId, request);
        return ResponseEntity.ok(CategoryResponseDto.of(updatedCategory));
    }

    @DeleteMapping(("/my/{categoryId}"))
    public ResponseEntity<DeleteCategoryResponseDto> deleteMyCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long categoryId
    ) {
        Long deletedCategoryId = categoryService.deleteMyCategory(userDetails.getUserId(), categoryId);
        return ResponseEntity.ok(DeleteCategoryResponseDto.of(deletedCategoryId));
    }
}
