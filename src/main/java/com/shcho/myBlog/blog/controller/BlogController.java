package com.shcho.myBlog.blog.controller;

import com.shcho.myBlog.blog.dto.BlogResponseDto;
import com.shcho.myBlog.blog.dto.BlogUpdateRequestDto;
import com.shcho.myBlog.blog.entity.Blog;
import com.shcho.myBlog.blog.service.BlogService;
import com.shcho.myBlog.user.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @GetMapping("/me")
    public ResponseEntity<BlogResponseDto> getMyBlog(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Blog myBlog = blogService.getMyBlogByUserId(userDetails.getUserId());
        return ResponseEntity.ok(BlogResponseDto.from(myBlog));
    }

    @GetMapping("/public/{nickname}")
    public ResponseEntity<BlogResponseDto> getBlogPageByNickname(
            @PathVariable String nickname
    ) {
        Blog userBlog = blogService.getUserBlogByNickname(nickname);
        return ResponseEntity.ok(BlogResponseDto.from(userBlog));
    }

    @PatchMapping("/me")
    public ResponseEntity<BlogResponseDto> updateMyBlog(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody BlogUpdateRequestDto requestDto
    ) {
        Blog updatedBlog = blogService.updateMyBlog(userDetails.getUserId(), requestDto);

        return ResponseEntity.ok(BlogResponseDto.from(updatedBlog));
    }

}
