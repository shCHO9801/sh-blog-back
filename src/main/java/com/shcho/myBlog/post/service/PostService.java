package com.shcho.myBlog.post.service;

import com.shcho.myBlog.blog.entity.Blog;
import com.shcho.myBlog.blog.repository.BlogRepository;
import com.shcho.myBlog.category.entity.Category;
import com.shcho.myBlog.category.repository.CategoryRepository;
import com.shcho.myBlog.libs.exception.CustomException;
import com.shcho.myBlog.post.dto.CreatePostRequestDto;
import com.shcho.myBlog.post.dto.PostThumbnailResponseDto;
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


    @Transactional
    public Post createMyPost(Long userId, CreatePostRequestDto requestDto) {

        Blog myBlog = blogRepository.findBlogByUserIdFetchUser(userId)
                .orElseThrow(() -> new CustomException(BLOG_NOT_FOUND));

        Category category;

        if (requestDto.categoryId() == null) {
            category = categoryRepository.findByBlogIdAndName(myBlog.getId(), DEFAULT_CATEGORY_NAME)
                    .orElseThrow(() -> new CustomException(DEFAULT_CATEGORY_NOT_FOUND));
        } else {
            category = categoryRepository.findById(requestDto.categoryId())
                    .orElseThrow(() -> new CustomException(CATEGORY_NOT_FOUND));

            if (!category.getBlog().getId().equals(myBlog.getId())) {
                throw new CustomException(CATEGORY_FORBIDDEN);
            }
        }

        // leaf 검증
        if (categoryRepository.existsByParent_Id(category.getId())) {
            throw new CustomException(POST_CAN_NOT_USE_NON_LEAF_CATEGORY);
        }

        String title = requestDto.title().trim();

        if(title.isBlank()) {
            throw new CustomException(TITLE_CAN_NOT_BLANK);
        }
        String content = requestDto.content();

        Post newPost = Post.of(myBlog, category, title, content, requestDto.isPublic());

        return postRepository.save(newPost);
    }

    public Page<PostThumbnailResponseDto> getPostsByUserNickname(String nickname, Pageable pageable) {
        return postQueryRepository.findPostThumbnailsByNickname(nickname, pageable);
    }

    public Page<PostThumbnailResponseDto> getPostsByUserNicknameAndKeyword(String nickname, String keyword, Pageable pageable) {
        if(keyword == null || keyword.trim().isBlank()){
            throw new CustomException(INVALID_KEYWORD);
        }

        return postQueryRepository.findPostThumbnailsByNicknameAndKeyword(nickname,keyword.trim(),pageable);
    }

    public Post getPostByNicknameAndPostId(String nickname, Long postId) {
        return postQueryRepository.findPostByNicknameAndPostId(nickname, postId)
                .orElseThrow(() -> new CustomException(POST_NOT_FOUND));
    }

    public Page<PostThumbnailResponseDto> getPostsByUserNicknameAndCategoryId(String nickname, Long categoryId, Pageable pageable) {
        return postQueryRepository.findPostThumbnailsByNicknameAndCategoryId(nickname, categoryId, pageable);
    }

    public Page<PostThumbnailResponseDto> getMyAllPosts(Long userId, Boolean publicPost, Pageable pageable) {
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
}
