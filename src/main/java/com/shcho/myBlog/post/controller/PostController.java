package com.shcho.myBlog.post.controller;

import com.shcho.myBlog.common.dto.PagedResponseDto;
import com.shcho.myBlog.post.dto.CreatePostRequestDto;
import com.shcho.myBlog.post.dto.CreatePostResponseDto;
import com.shcho.myBlog.post.dto.PostResponseDto;
import com.shcho.myBlog.post.dto.PostThumbnailResponseDto;
import com.shcho.myBlog.post.entity.Post;
import com.shcho.myBlog.post.service.PostService;
import com.shcho.myBlog.user.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
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

    @GetMapping("/public/{nickname}")
    public ResponseEntity<PagedResponseDto<PostThumbnailResponseDto>> getAllPostsByUserNickname(
            @PathVariable String nickname,
            Pageable pageable
    ) {
        Page<PostThumbnailResponseDto> getAllPostsByNickname =
                postService.getPostsByUserNickname(nickname, pageable);
        return ResponseEntity.ok(PagedResponseDto.from(getAllPostsByNickname));
    }

    @GetMapping("/public/{nickname}/search")
    public ResponseEntity<PagedResponseDto<PostThumbnailResponseDto>> getAllPostsByNicknameAndKeyword(
            @PathVariable String nickname,
            @RequestParam String keyword,
            Pageable pageable
    ) {
        Page<PostThumbnailResponseDto> getAllPostsByNicknameAndKeyword =
                postService.getPostsByUserNicknameAndKeyword(nickname, keyword, pageable);
        return ResponseEntity.ok(PagedResponseDto.from(getAllPostsByNicknameAndKeyword));
    }

    @GetMapping("/public/{nickname}/posts/{postId}")
    public ResponseEntity<PostResponseDto> getPostByNicknameAndPostId (
            @PathVariable String nickname,
            @PathVariable Long postId
    ) {
        Post post = postService.getPostByNicknameAndPostId(nickname, postId);
        return ResponseEntity.ok(PostResponseDto.from(post));
    }

    @GetMapping("/public/{nickname}/categories/{categoryId}")
    public ResponseEntity<PagedResponseDto<PostThumbnailResponseDto>> getAllPostsByCategoryId(
            @PathVariable String nickname,
            @PathVariable Long categoryId,
            Pageable pageable
    ) {
        Page<PostThumbnailResponseDto> getAllPostsByCategoryId =
                postService.getPostsByUserNicknameAndCategoryId(nickname, categoryId, pageable);
        return ResponseEntity.ok(PagedResponseDto.from(getAllPostsByCategoryId));
    }
}
