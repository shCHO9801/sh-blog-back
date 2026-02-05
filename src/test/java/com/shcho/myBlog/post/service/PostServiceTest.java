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
import com.shcho.myBlog.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static com.shcho.myBlog.libs.exception.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Post Service Unit Test")
@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    private static final String DEFAULT_CATEGORY_NAME = "미분류";

    @Mock
    private PostRepository postRepository;
    @Mock
    private BlogRepository blogRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private PostQueryRepository postQueryRepository;
    @InjectMocks
    private PostService postService;

    @Test
    @DisplayName("게시글 생성 성공")
    void createPostSuccess() {
        // given
        Long userId = 1L;
        User user = User.builder().userId(userId).build();
        Blog blog = Blog.builder().id(1L).user(user).build();
        Category category = Category.builder().id(1L).blog(blog).build();
        String rawTitle = "  newPost  ";


        CreatePostRequestDto requestDto = new CreatePostRequestDto(
                rawTitle, "new Post Content", category.getId(), true
        );

        when(blogRepository.findBlogByUserIdFetchUser(userId))
                .thenReturn(Optional.of(blog));
        when(categoryRepository.findById(category.getId()))
                .thenReturn(Optional.of(category));
        when(categoryRepository.existsByParent_Id(category.getId()))
                .thenReturn(false);
        when(postRepository.save(any(Post.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        Post newPost = postService.createMyPost(userId, requestDto);

        // then
        assertNotNull(newPost);
        assertEquals(rawTitle.trim(), newPost.getTitle());
        assertEquals(requestDto.content(), newPost.getContent());
        assertEquals(requestDto.categoryId(), newPost.getCategory().getId());

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository, times(1)).save(captor.capture());

        Post saved = captor.getValue();
        assertEquals(rawTitle.trim(), saved.getTitle());
        assertEquals("new Post Content", saved.getContent());
        assertEquals(category.getId(), saved.getCategory().getId());
        assertEquals(blog.getId(), saved.getBlog().getId());
    }

    @Test
    @DisplayName("게시글 생성 성공 - 카테고리 설정 안함")
    void createPostSuccessWithoutCategory() {
        // given
        Long userId = 1L;
        User user = User.builder().userId(userId).build();
        Blog blog = Blog.builder().id(1L).user(user).build();
        Category defaultCategory = Category.builder().id(1L).name(DEFAULT_CATEGORY_NAME).blog(blog).build();
        CreatePostRequestDto requestDto = new CreatePostRequestDto(
                "newPostWithoutCategory", "new Post Content", null, true
        );

        when(blogRepository.findBlogByUserIdFetchUser(userId))
                .thenReturn(Optional.of(blog));
        when(categoryRepository.findByBlogIdAndName(blog.getId(), DEFAULT_CATEGORY_NAME))
                .thenReturn(Optional.of(defaultCategory));
        when(categoryRepository.existsByParent_Id(defaultCategory.getId()))
                .thenReturn(false);
        when(postRepository.save(any(Post.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        Post newPost = postService.createMyPost(userId, requestDto);

        // then
        assertNotNull(newPost);
        assertEquals(requestDto.title().trim(), newPost.getTitle());
        assertEquals(requestDto.content(), newPost.getContent());
        assertEquals(defaultCategory.getId(), newPost.getCategory().getId());

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postRepository, times(1)).save(captor.capture());

        Post saved = captor.getValue();
        assertEquals(requestDto.title().trim(), saved.getTitle());
        assertEquals("new Post Content", saved.getContent());
        assertEquals(defaultCategory.getId(), saved.getCategory().getId());
        assertEquals(blog.getId(), saved.getBlog().getId());
    }

    @Test
    @DisplayName("게시글 생성 실패 - 소유권이 없는 카테고리")
    void createPostFailedForbiddenCategory() {
        // given
        Long userId = 1L;
        User user = User.builder().userId(userId).build();
        User otherUser = User.builder().userId(2L).build();
        Blog blog = Blog.builder().id(1L).user(user).build();
        Blog otherBlog = Blog.builder().id(2L).user(otherUser).build();
        Category otherCategory = Category.builder().id(1L).blog(otherBlog).build();

        CreatePostRequestDto requestDto = new CreatePostRequestDto(
                "newPost", "new Post Content", otherCategory.getId(), true
        );

        when(blogRepository.findBlogByUserIdFetchUser(userId))
                .thenReturn(Optional.of(blog));
        when(categoryRepository.findById(requestDto.categoryId()))
                .thenReturn(Optional.of(otherCategory));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> postService.createMyPost(userId, requestDto));

        assertEquals(CATEGORY_FORBIDDEN, exception.getErrorCode());
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시글 생성 실패 - 리프 카테고리가 아닌 카테고리")
    void createPostFailedPostCanNotUseNonLeafCategory() {
        // given
        Long userId = 1L;
        User user = User.builder().userId(userId).build();
        Blog blog = Blog.builder().id(1L).user(user).build();
        Category nonLeafCategory = Category.builder().id(1L).blog(blog).build();

        CreatePostRequestDto requestDto = new CreatePostRequestDto(
                "newPost", "new Post Content", nonLeafCategory.getId(), true
        );

        when(blogRepository.findBlogByUserIdFetchUser(userId))
                .thenReturn(Optional.of(blog));
        when(categoryRepository.findById(requestDto.categoryId()))
                .thenReturn(Optional.of(nonLeafCategory));
        when(categoryRepository.existsByParent_Id(nonLeafCategory.getId()))
                .thenReturn(true);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> postService.createMyPost(userId, requestDto));

        assertEquals(POST_CAN_NOT_USE_NON_LEAF_CATEGORY, exception.getErrorCode());
        verify(postRepository, never()).save(any());
    }

    @Test
    @DisplayName("게시글 생성 실패 - 제목 trim 후 blank 일 때 예외 발생")
    void createPostFailedTitleCanNotBlank() {
        // given
        Long userId = 1L;
        User user = User.builder().userId(userId).build();
        Blog blog = Blog.builder().id(1L).user(user).build();
        Category category = Category.builder().id(1L).blog(blog).build();

        CreatePostRequestDto requestDto = new CreatePostRequestDto(
                "    ", "content", category.getId(), true
        );

        when(blogRepository.findBlogByUserIdFetchUser(userId))
                .thenReturn(Optional.of(blog));
        when(categoryRepository.findById(requestDto.categoryId()))
                .thenReturn(Optional.of(category));
        when(categoryRepository.existsByParent_Id(category.getId()))
                .thenReturn(false);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> postService.createMyPost(userId, requestDto));

        assertEquals(TITLE_CAN_NOT_BLANK, exception.getErrorCode());
        verify(blogRepository, never()).save(any());

        verify(blogRepository, times(1))
                .findBlogByUserIdFetchUser(userId);
        verify(categoryRepository, times(1))
                .findById(category.getId());
        verify(categoryRepository, times(1))
                .existsByParent_Id(category.getId());
    }

    @Test
    @DisplayName("닉네임으로 게시글 전체 조회 성공")
    void getPostsByUserNicknameSuccess() {
        // given
        String nickname = "user1";
        Pageable pageable = PageRequest.of(0, 10);

        PostThumbnailResponseDto dto1 = buildPostThumbnailResponseDto(1L, "t1");
        PostThumbnailResponseDto dto2 = buildPostThumbnailResponseDto(2L, "t2");

        Page<PostThumbnailResponseDto> dtoPages =
                new PageImpl<>(List.of(dto1, dto2), pageable, 2);

        when(postQueryRepository.findPostThumbnailsByNickname(nickname, pageable))
                .thenReturn(dtoPages);

        // when
        Page<PostThumbnailResponseDto> result = postService.getPostsByUserNickname(nickname, pageable);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).postId());
        assertEquals(dto1.title(), result.getContent().get(0).title());

        verify(postQueryRepository, times(1))
                .findPostThumbnailsByNickname(nickname, pageable);
        verifyNoMoreInteractions(postQueryRepository);
    }

    @Test
    @DisplayName("닉네임과 키워드로 게시글 검색 성공")
    void getPostsByUserNicknameAndKeywordSuccess() {
        String nickname = "user1";
        String keyword = "검색";
        Pageable pageable = PageRequest.of(0, 10);

        PostThumbnailResponseDto dto1 = buildPostThumbnailResponseDto(1L, "검색용 게시글");
        PostThumbnailResponseDto dto2 = buildPostThumbnailResponseDto(2L, "제목");
        PostThumbnailResponseDto dto3 = buildPostThumbnailResponseDto(3L, "title");
        PostThumbnailResponseDto dto4 = buildPostThumbnailResponseDto(4L, "title 검색");

        Page<PostThumbnailResponseDto> dtoPages =
                new PageImpl<>(List.of(dto1, dto2, dto3, dto4), pageable, 4);

        when(postQueryRepository.findPostThumbnailsByNicknameAndKeyword(nickname, keyword, pageable))
                .thenReturn(dtoPages);

        // when
        Page<PostThumbnailResponseDto> result =
                postService.getPostsByUserNicknameAndKeyword(nickname, keyword, pageable);

        // then
        assertNotNull(result);
        assertEquals(4, result.getTotalElements());
        assertEquals(4, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).postId());

        verify(postQueryRepository, times(1))
                .findPostThumbnailsByNicknameAndKeyword(nickname, keyword, pageable);
        verifyNoMoreInteractions(postQueryRepository);
    }

    @Test
    @DisplayName("닉네임과 키워드로 게시글 조회 실패 - 키워드 공백")
    void getPostsByUserNicknameAndKeywordFailedBlankKeyword() {
        // given
        String nickname = "user1";
        String keyword = "   ";
        Pageable pageable = PageRequest.of(0, 10);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> postService.getPostsByUserNicknameAndKeyword(nickname, keyword, pageable));

        assertEquals(INVALID_KEYWORD, exception.getErrorCode());
        verifyNoInteractions(postQueryRepository);
    }

    @Test
    @DisplayName("닉네임과 키워드로 게시글 조회 실패 - 키워드 null")
    void getPostsByUserNicknameAndKeywordFailedNullKeyword() {
        // given
        String nickname = "user1";
        Pageable pageable = PageRequest.of(0, 10);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> postService.getPostsByUserNicknameAndKeyword(nickname, null, pageable));

        assertEquals(INVALID_KEYWORD, exception.getErrorCode());
        verifyNoInteractions(postQueryRepository);
    }

    @Test
    @DisplayName("닉네임과 게시글 Id로 게시글 조회")
    void getPostByNicknameAndPostIdSuccess() {
        // given
        String nickname = "user1";
        Long postId = 1L;
        Post post = Post.builder().id(postId).build();

        when(postQueryRepository.findPostByNicknameAndPostId(nickname, postId))
                .thenReturn(Optional.of(post));

        // when
        Post result = postService.getPostByNicknameAndPostId(nickname, postId);

        // then
        assertNotNull(result);
        assertEquals(postId, result.getId());
        verify(postQueryRepository, times(1))
                .findPostByNicknameAndPostId(nickname, postId);
    }

    @Test
    @DisplayName("닉네임과 게시글 Id로 게시글 조회 실패 - 존재하지 않는 게시글")
    void getPostByNicknameAndPostIdFailedPostNotFound() {
        // given
        String nickname = "user1";
        Long postId = 1L;

        when(postQueryRepository.findPostByNicknameAndPostId(nickname, postId))
                .thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> postService.getPostByNicknameAndPostId(nickname, postId));

        // then
        assertEquals(POST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("닉네임과 카테고리 Id로 게시글 전체 조회 성공")
    void getPostsByUserNicknameAndCategoryIdSuccess() {
        // given
        String nickname = "user1";
        Long categoryId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        PostThumbnailResponseDto dto1 = buildPostThumbnailResponseDto(1L, "t1");
        PostThumbnailResponseDto dto2 = buildPostThumbnailResponseDto(2L, "t2");
        PostThumbnailResponseDto dto3 = buildPostThumbnailResponseDto(3L, "t3");
        PostThumbnailResponseDto dto4 = buildPostThumbnailResponseDto(4L, "t4");

        Page<PostThumbnailResponseDto> dtoPages =
                new PageImpl<>(List.of(dto1, dto2, dto3, dto4));

        when(postQueryRepository.findPostThumbnailsByNicknameAndCategoryId(nickname, categoryId, pageable))
                .thenReturn(dtoPages);

        // when
        Page<PostThumbnailResponseDto> result =
                postService.getPostsByUserNicknameAndCategoryId(nickname, categoryId, pageable);

        // then
        assertNotNull(result);
        assertEquals(4, result.getTotalElements());
        assertEquals(4, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).postId());

        verify(postQueryRepository, times(1))
                .findPostThumbnailsByNicknameAndCategoryId(nickname, categoryId, pageable);
    }

    @Test
    @DisplayName("내 게시글 전체 조회")
    void getMyAllPostsSuccess() {
        // given
        Long userId = 1L;
        User user = User.builder().userId(userId).build();
        Blog blog = Blog.builder().id(1L).user(user).build();

        Boolean publicPost = true;
        Pageable pageable = PageRequest.of(0, 10);

        PostThumbnailResponseDto dto1 = buildPostThumbnailResponseDto(1L, "t1");
        PostThumbnailResponseDto dto2 = buildPostThumbnailResponseDto(2L, "t2");
        PostThumbnailResponseDto dto3 = buildPostThumbnailResponseDto(3L, "t3");
        PostThumbnailResponseDto dto4 = buildPostThumbnailResponseDto(4L, "t4");

        Page<PostThumbnailResponseDto> dtoPages =
                new PageImpl<>(List.of(dto1, dto2, dto3, dto4));

        when(blogRepository.findBlogByUserIdFetchUser(userId))
                .thenReturn(Optional.of(blog));
        when(postQueryRepository.getMyAllPosts(blog.getId(), publicPost, pageable))
                .thenReturn(dtoPages);

        // when
        Page<PostThumbnailResponseDto> result =
                postService.getMyAllPosts(userId, publicPost, pageable);

        // then
        assertNotNull(result);
        assertEquals(4, result.getTotalElements());
        assertEquals(4, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).postId());
        verify(blogRepository, times(1))
                .findBlogByUserIdFetchUser(userId);
        verify(postQueryRepository, times(1))
                .getMyAllPosts(blog.getId(), publicPost, pageable);
    }

    @Test
    @DisplayName("내 게시글 전체 조회 실패 - 존재하지 않는 블로그")
    void getMyAllPostsFailedBlogNotFound() {
        // given
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        when(blogRepository.findBlogByUserIdFetchUser(userId))
                .thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> postService.getMyAllPosts(userId, true, pageable));

        assertEquals(BLOG_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("내 게시글 단건 조회 성공")
    void getMyPostByPostIdSuccess() {
        // given
        Long postId = 1L;
        Long userId = 1L;
        User user = User.builder().userId(userId).build();
        Blog blog = Blog.builder().id(1L).user(user).build();
        Post post = Post.builder().id(postId).blog(blog).build();

        when(blogRepository.findBlogByUserIdFetchUser(userId))
                .thenReturn(Optional.of(blog));

        when(postQueryRepository.getMyPostByBlog(blog.getId(), postId))
                .thenReturn(Optional.of(post));

        // when
        Post result = postService.getMyPostByPostId(userId, postId);

        // then
        assertNotNull(result);
        assertEquals(postId, result.getId());
        verify(blogRepository, times(1))
                .findBlogByUserIdFetchUser(userId);
        verify(postQueryRepository, times(1))
                .getMyPostByBlog(blog.getId(), postId);
    }

    private PostThumbnailResponseDto buildPostThumbnailResponseDto(
            Long id, String title
    ) {
        return new PostThumbnailResponseDto(id, title, true, null);
    }
}