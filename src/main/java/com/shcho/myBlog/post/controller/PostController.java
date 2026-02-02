package com.shcho.myBlog.post.controller;

import com.shcho.myBlog.post.dto.CreatePostRequestDto;
import com.shcho.myBlog.post.dto.CreatePostResponseDto;
import com.shcho.myBlog.post.entity.Post;
import com.shcho.myBlog.post.service.PostService;
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
@RequestMapping("/api/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<CreatePostResponseDto> createMyPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreatePostRequestDto requestDto
    ) {
        Post newPost = postService.createMyPost(userDetails.getUserId(), requestDto);
        return ResponseEntity.ok(CreatePostResponseDto.from(newPost));
    }
}
