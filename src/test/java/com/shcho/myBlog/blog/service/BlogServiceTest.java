package com.shcho.myBlog.blog.service;

import com.shcho.myBlog.blog.entity.Blog;
import com.shcho.myBlog.blog.repository.BlogRepository;
import com.shcho.myBlog.libs.exception.CustomException;
import com.shcho.myBlog.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.shcho.myBlog.libs.exception.ErrorCode.BLOG_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Blog Service Unit Test")
@ExtendWith(MockitoExtension.class)
class BlogServiceTest {

    @Mock private BlogRepository blogRepository;
    @InjectMocks private BlogService blogService;

    @Test
    @DisplayName("내 Blog 조회 성공")
    void getMyBlogSuccess() {
        // given
        Long userId = 1L;

        User user = User.builder()
                .userId(userId)
                .nickname("nickname")
                .build();

        Blog blog = Blog.builder()
                .id(1L)
                .title("nickname의 블로그")
                .intro("")
                .bannerImageUrl(null)
                .user(user)
                .build();

        when(blogRepository.findBlogByUserIdFetchUser(userId))
                .thenReturn(Optional.of(blog));

        // when
        Blog result = blogService.getMyBlogByUserId(userId);

        // then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("nickname의 블로그", result.getTitle());
        assertEquals(user, result.getUser());

        verify(blogRepository, times(1)).findBlogByUserIdFetchUser(userId);
        verifyNoMoreInteractions(blogRepository);
    }

    @Test
    @DisplayName("내 Blog 조회 실패 - BLOG_NOT_FOUND")
    void getMyBlogFailureBlogNotFound() {
        // given
        Long userId = 999L;

        when(blogRepository.findBlogByUserIdFetchUser(userId))
                .thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> blogService.getMyBlogByUserId(userId));

        assertEquals(BLOG_NOT_FOUND, exception.getErrorCode());
        verify(blogRepository, times(1)).findBlogByUserIdFetchUser(userId);
        verifyNoMoreInteractions(blogRepository);
    }

    @Test
    @DisplayName("공개 Blog 조회 성공 - nickname")
    void getUserBlogByNicknameSuccess() {
        // given
        String nickname = "nickname";

        User user = User.builder()
                .userId(1L)
                .nickname(nickname)
                .build();

        Blog blog = Blog.builder()
                .id(1L)
                .title(nickname + "의 블로그")
                .intro("")
                .bannerImageUrl(null)
                .user(user)
                .build();

        when(blogRepository.findBlogByUserNicknameFetchUser(nickname))
                .thenReturn(Optional.of(blog));

        // when
        Blog result = blogService.getUserBlogByNickname(nickname);

        // then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(nickname + "의 블로그", result.getTitle());
        assertEquals(user, result.getUser());

        verify(blogRepository, times(1)).findBlogByUserNicknameFetchUser(nickname);
        verifyNoMoreInteractions(blogRepository);
    }

    @Test
    @DisplayName("공개 Blog 조회 실패 - BLOG_NOT_FOUND")
    void getUserBlogByNicknameFailureBlogNotFound() {
        // given
        String nickname = "unknownNickname";

        when(blogRepository.findBlogByUserNicknameFetchUser(nickname))
                .thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> blogService.getUserBlogByNickname(nickname));

        assertEquals(BLOG_NOT_FOUND, exception.getErrorCode());
        verify(blogRepository, times(1)).findBlogByUserNicknameFetchUser(nickname);
        verifyNoMoreInteractions(blogRepository);
    }
}