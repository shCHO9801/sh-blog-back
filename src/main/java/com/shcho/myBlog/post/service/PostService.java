package com.shcho.myBlog.post.service;

import com.shcho.myBlog.blog.entity.Blog;
import com.shcho.myBlog.blog.repository.BlogRepository;
import com.shcho.myBlog.category.entity.Category;
import com.shcho.myBlog.category.repository.CategoryRepository;
import com.shcho.myBlog.common.entity.UploadFile;
import com.shcho.myBlog.common.service.MinioService;
import com.shcho.myBlog.common.service.UploadFileService;
import com.shcho.myBlog.libs.exception.CustomException;
import com.shcho.myBlog.post.dto.*;
import com.shcho.myBlog.post.entity.Post;
import com.shcho.myBlog.post.repository.PostQueryRepository;
import com.shcho.myBlog.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.shcho.myBlog.libs.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final String DEFAULT_CATEGORY_NAME = "미분류";
    private final PostRepository postRepository;
    private final BlogRepository blogRepository;
    private final CategoryRepository categoryRepository;
    private final PostQueryRepository postQueryRepository;
    private final UploadFileService uploadFileService;
    private final MinioService minioService;


    @Transactional
    public Post createMyPost(Long userId, CreatePostRequestDto requestDto) {

        Blog myBlog = blogRepository.findBlogByUserIdFetchUser(userId)
                .orElseThrow(() -> new CustomException(BLOG_NOT_FOUND));

        Category category = validateCategory(requestDto.categoryId(), myBlog.getId());
        String title = validateTitle(requestDto.title());
        String content = requestDto.content();

        Post newPost = Post.of(myBlog, category, title, content, requestDto.isPublic());

        Post saved = postRepository.save(newPost);

        uploadFileService.attachFilesToPost(userId, saved.getId(), saved.getContent());

        return saved;
    }

    public Page<PostThumbnailResponseDto> getPostsByUserNickname(String nickname, Pageable pageable) {
        return postQueryRepository.findPostThumbnailsByNickname(nickname, pageable);
    }

    public Page<PostThumbnailResponseDto> getPostsByUserNicknameAndKeyword(
            String nickname, String keyword, Pageable pageable
    ) {
        if (keyword == null || keyword.trim().isBlank()) {
            throw new CustomException(INVALID_KEYWORD);
        }

        return postQueryRepository
                .findPostThumbnailsByNicknameAndKeyword(nickname, keyword.trim(), pageable);
    }

    public Post getPostByNicknameAndPostId(String nickname, Long postId) {
        return postQueryRepository.findPostByNicknameAndPostId(nickname, postId)
                .orElseThrow(() -> new CustomException(POST_NOT_FOUND));
    }

    public Page<PostThumbnailResponseDto> getPostsByUserNicknameAndCategoryId(
            String nickname, Long categoryId, Pageable pageable
    ) {
        return postQueryRepository.findPostThumbnailsByNicknameAndCategoryId(nickname, categoryId, pageable);
    }

    public Page<PostThumbnailResponseDto> getMyAllPosts(
            Long userId, Boolean publicPost, Pageable pageable
    ) {
        Blog myBlog = blogRepository.findBlogByUserIdFetchUser(userId)
                .orElseThrow(() -> new CustomException(BLOG_NOT_FOUND));

        return postQueryRepository.getMyAllPosts(myBlog.getId(), publicPost, pageable);
    }

    public Post getMyPostByPostId(Long userId, Long postId) {
        Blog myBlog = blogRepository.findBlogByUserIdFetchUser(userId)
                .orElseThrow(() -> new CustomException(BLOG_NOT_FOUND));

        return postQueryRepository.getMyPostByBlog(myBlog.getId(), postId)
                .orElseThrow(() -> new CustomException(POST_NOT_FOUND));
    }

    @Transactional
    public Post updateTitle(Long userId, Long postId, UpdatePostTitleRequestDto requestDto) {
        Post post = getMyPostByPostId(userId, postId);
        String title = validateTitle(requestDto.title());

        post.setTitle(title);

        return post;
    }

    @Transactional
    public Post updateContent(Long userId, Long postId, UpdatePostContentRequestDto requestDto) {
        Post post = getMyPostByPostId(userId, postId);
        String content = requestDto.content();

        var newFids = uploadFileService.extractFids(content);
        var attachedFiles = uploadFileService.getAttachedFilesByPostId(post.getId());
        var toDelete = attachedFiles.stream()
                        .filter(f -> !newFids.contains(f.getId()))
                                .toList();

        for(UploadFile file : toDelete) {
            minioService.deleteObject(file.getObjectName());
        }
        uploadFileService.markDeletedAndSaveAll(toDelete);

        post.setContent(content);

        uploadFileService.attachFilesToPost(userId, post.getId(), content);

        return post;
    }

    @Transactional
    public Post updateCategory(Long userId, Long postId, UpdatePostCategoryRequestDto requestDto) {
        Post post = getMyPostByPostId(userId, postId);
        Category category = validateCategory(requestDto.categoryId(), post.getBlog().getId());

        post.setCategory(category);

        return post;
    }

    @Transactional
    public Post updatePublic(Long userId, Long postId, UpdatePostPublicRequestDto requestDto) {
        Post post = getMyPostByPostId(userId, postId);
        post.setPublic(requestDto.isPublic());
        return post;
    }

    @Transactional
    public Long deletePost(Long userId, Long postId) {
        Post post = getMyPostByPostId(userId, postId);
        Long id = post.getId();

        var attachedFiles = uploadFileService.getAttachedFilesByPostId(id);

        for(UploadFile attachedFile : attachedFiles) {
            minioService.deleteObject(attachedFile.getObjectName());
        }
        uploadFileService.markDeletedAndSaveAll(attachedFiles);

        postRepository.delete(post);
        return id;
    }

    private String validateTitle(String title) {
        title = title.trim();

        if (title.isBlank()) {
            throw new CustomException(TITLE_CAN_NOT_BLANK);
        }

        return title;
    }

    private Category validateCategory(Long categoryId, Long blogId) {
        Category category;

        if (categoryId == null) {
            category = categoryRepository.findByBlogIdAndName(blogId, DEFAULT_CATEGORY_NAME)
                    .orElseThrow(() -> new CustomException(DEFAULT_CATEGORY_NOT_FOUND));
        } else {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CustomException(CATEGORY_NOT_FOUND));

            if (!category.getBlog().getId().equals(blogId)) {
                throw new CustomException(CATEGORY_FORBIDDEN);
            }
        }

        if (categoryRepository.existsByParent_Id(category.getId())) {
            throw new CustomException(POST_CAN_NOT_USE_NON_LEAF_CATEGORY);
        }

        return category;
    }
}
