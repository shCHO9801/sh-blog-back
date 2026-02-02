package com.shcho.myBlog.post.service;

import com.shcho.myBlog.blog.entity.Blog;
import com.shcho.myBlog.blog.repository.BlogRepository;
import com.shcho.myBlog.category.entity.Category;
import com.shcho.myBlog.category.repository.CategoryRepository;
import com.shcho.myBlog.libs.exception.CustomException;
import com.shcho.myBlog.post.dto.CreatePostRequestDto;
import com.shcho.myBlog.post.entity.Post;
import com.shcho.myBlog.post.repository.PostRepository;
import com.shcho.myBlog.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.shcho.myBlog.libs.exception.ErrorCode.CATEGORY_FORBIDDEN;
import static com.shcho.myBlog.libs.exception.ErrorCode.POST_CAN_NOT_USE_NON_LEAF_CATEGORY;
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
                rawTitle, "new Post Content", category.getId()
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
                "newPostWithoutCategory", "new Post Content", null
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
                "newPost", "new Post Content", otherCategory.getId()
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
                "newPost", "new Post Content", nonLeafCategory.getId()
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
}