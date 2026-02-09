package com.shcho.myBlog.post.controller;

import com.shcho.myBlog.common.dto.PagedResponseDto;
import com.shcho.myBlog.post.dto.*;
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
    public ResponseEntity<PostResponseDto> getPostByNicknameAndPostId(
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

    @GetMapping("/my")
    public ResponseEntity<PagedResponseDto<PostThumbnailResponseDto>> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Boolean publicPost,
            Pageable pageable
    ) {
        Page<PostThumbnailResponseDto> getMyAllPosts =
                postService.getMyAllPosts(userDetails.getUserId(), publicPost, pageable);
        return ResponseEntity.ok(PagedResponseDto.from(getMyAllPosts));
    }

    @GetMapping("/my/posts/{postId}")
    public ResponseEntity<PostResponseDto> getMyPostByPostId(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        Post post = postService.getMyPostByPostId(userDetails.getUserId(), postId);
        return ResponseEntity.ok(PostResponseDto.from(post));
    }

    @PatchMapping("/my/posts/{postId}/title")
    public ResponseEntity<PostResponseDto> updateMyPostTitle(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostTitleRequestDto requestDto
    ) {
        Post post = postService.updateTitle(userDetails.getUserId(), postId, requestDto);
        return ResponseEntity.ok(PostResponseDto.from(post));
    }

    @PatchMapping("/my/posts/{postId}/content")
    public ResponseEntity<PostResponseDto> updateMyPostContent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestBody UpdatePostContentRequestDto requestDto
    ) {
        Post post = postService.updateContent(userDetails.getUserId(), postId, requestDto);
        return ResponseEntity.ok(PostResponseDto.from(post));
    }

    @PatchMapping("/my/posts/{postId}/category")
    public ResponseEntity<PostResponseDto> updateMyPostCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @RequestBody UpdatePostCategoryRequestDto requestDto
    ) {
        Post post = postService.updateCategory(userDetails.getUserId(), postId, requestDto);
        return ResponseEntity.ok(PostResponseDto.from(post));
    }

    @PatchMapping("/my/posts/{postId}/public")
    public ResponseEntity<PostResponseDto> updateMyPostPublic(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostPublicRequestDto requestDto
    ) {
        Post post = postService.updatePublic(userDetails.getUserId(), postId, requestDto);
        return ResponseEntity.ok(PostResponseDto.from(post));
    }

    @DeleteMapping("/my/posts/{postId}")
    public ResponseEntity<DeletePostResponseDto> deleteMyPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long postId
    ) {
        Long deletedPostId = postService.deletePost(userDetails.getUserId(), postId);
        return ResponseEntity.ok(DeletePostResponseDto.from(deletedPostId));
    }
}
