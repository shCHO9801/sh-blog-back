package com.shcho.myBlog.category.service;

import com.shcho.myBlog.blog.entity.Blog;
import com.shcho.myBlog.blog.repository.BlogRepository;
import com.shcho.myBlog.category.dto.CreateCategoryRequestDto;
import com.shcho.myBlog.category.entity.Category;
import com.shcho.myBlog.category.repository.CategoryRepository;
import com.shcho.myBlog.libs.exception.CustomException;
import com.shcho.myBlog.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.shcho.myBlog.libs.exception.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("Category Service Unit Test")
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BlogRepository blogRepository;
    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("카테고리 생성 성공")
    void createCategorySuccess() {
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

        String categoryName = "newCategory";
        String description = "newDescription";
        Long parentId = null;

        CreateCategoryRequestDto requestDto = createTestRequest(categoryName, description, parentId);
        when(blogRepository.findBlogByUserIdFetchUser(userId))
                .thenReturn(Optional.of(blog));
        when(categoryRepository.existsByBlogIdAndParentIdAndName(blog.getId(), parentId, categoryName))
                .thenReturn(false);
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        Category newCategory = categoryService.createMyCategory(userId, requestDto);

        // then
        assertNotNull(newCategory);
        assertEquals(categoryName, newCategory.getName());
        assertTrue(newCategory.isRoot());
        assertEquals(blog.getId(), newCategory.getBlog().getId());
    }

    @Test
    @DisplayName("카테고리 생성 성공 - name trim 적용")
    void createCategorySuccessTrimApplied() {
        // given
        Long userId = 1L;

        User user = User.builder().userId(userId).nickname("nickname").build();
        Blog blog = Blog.builder().id(1L).user(user).build();

        String rawName = "  newCategory  ";
        String trimmedName = "newCategory";

        CreateCategoryRequestDto requestDto = createTestRequest(rawName, "desc", null);

        when(blogRepository.findBlogByUserIdFetchUser(userId)).thenReturn(Optional.of(blog));
        when(categoryRepository.existsByBlogIdAndParentIdAndName(blog.getId(), null, trimmedName))
                .thenReturn(false);
        when(categoryRepository.save(any(Category.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        Category newCategory = categoryService.createMyCategory(userId, requestDto);

        // then
        assertEquals(trimmedName, newCategory.getName());
    }

    @Test
    @DisplayName("카테고리 생성 실패 - 부모가 root가 아니면 2단계 제한")
    void createCategoryFailedParentNotRoot() {
        // given
        Long userId = 1L;

        User user = User.builder().userId(userId).nickname("nickname").build();
        Blog blog = Blog.builder().id(1L).user(user).build();

        // parent가 root가 아니게 만들기: parent.parent != null
        Category grandParent = Category.builder().id(10L).blog(blog).name("개발").build();
        Category parent = Category.builder().id(11L).blog(blog).name("Java").parent(grandParent).build();

        CreateCategoryRequestDto requestDto = createTestRequest("Spring", "desc", parent.getId());

        when(blogRepository.findBlogByUserIdFetchUser(userId)).thenReturn(Optional.of(blog));
        when(categoryRepository.findById(parent.getId())).thenReturn(Optional.of(parent));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> categoryService.createMyCategory(userId, requestDto));

        assertEquals(CATEGORY_PARENT_DEPTH_EXCEEDED, exception.getErrorCode());
    }

    @Test
    @DisplayName("카테고리 생성 실패 - 미분류 카테고리는 자식을 가질 수 없음")
    void createCategoryFailedParentIsDefaultCategory() {
        // given
        Long userId = 1L;

        User user = User.builder().userId(userId).nickname("nickname").build();
        Blog blog = Blog.builder().id(1L).user(user).build();

        Category parent = Category.builder()
                .id(11L)
                .blog(blog)
                .name("미분류")
                .parent(null)
                .build();

        CreateCategoryRequestDto requestDto = createTestRequest("Spring", "desc", parent.getId());

        when(blogRepository.findBlogByUserIdFetchUser(userId)).thenReturn(Optional.of(blog));
        when(categoryRepository.findById(parent.getId())).thenReturn(Optional.of(parent));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> categoryService.createMyCategory(userId, requestDto));

        assertEquals(CATEGORY_CANNOT_HAVE_CHILDREN, exception.getErrorCode());
    }

    @Test
    @DisplayName("카테고리 생성 실패 - 내 블로그가 아닌 부모 카테고리로 생성 불가")
    void createCategoryFailedParentNotInMyBlog() {
        // given
        Long userId = 1L;

        User user = User.builder().userId(userId).nickname("nickname").build();
        Blog myBlog = Blog.builder().id(1L).user(user).build();

        Blog otherBlog = Blog.builder().id(2L).build();
        Category parent = Category.builder()
                .id(11L)
                .blog(otherBlog)
                .name("개발")
                .parent(null) // root
                .build();

        CreateCategoryRequestDto requestDto = createTestRequest("Spring", "desc", parent.getId());

        when(blogRepository.findBlogByUserIdFetchUser(userId)).thenReturn(Optional.of(myBlog));
        when(categoryRepository.findById(parent.getId())).thenReturn(Optional.of(parent));

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> categoryService.createMyCategory(userId, requestDto));

        assertEquals(CATEGORY_FORBIDDEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("카테고리 생성 실패 - 같은 부모 아래 중복 이름이면 실패")
    void createCategoryFailedDuplicateNameUnderSameParent() {
        // given
        Long userId = 1L;

        User user = User.builder().userId(userId).nickname("nickname").build();
        Blog blog = Blog.builder().id(1L).user(user).build();

        Category parent = Category.builder()
                .id(11L)
                .blog(blog)
                .name("개발")
                .parent(null) // root
                .build();

        CreateCategoryRequestDto requestDto = createTestRequest("Spring", "desc", parent.getId());

        when(blogRepository.findBlogByUserIdFetchUser(userId)).thenReturn(Optional.of(blog));
        when(categoryRepository.findById(parent.getId())).thenReturn(Optional.of(parent));
        when(categoryRepository.existsByBlogIdAndParentIdAndName(blog.getId(), parent.getId(), "Spring"))
                .thenReturn(true);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> categoryService.createMyCategory(userId, requestDto));

        assertEquals(DUPLICATED_CATEGORY_NAME, exception.getErrorCode());
    }

    @Test
    @DisplayName("카테고리 생성 실패 - 내 블로그가 없으면 실패")
    void createCategoryFailedBlogNotFound() {
        // given
        Long userId = 1L;
        CreateCategoryRequestDto requestDto = createTestRequest("Spring", "desc", null);

        when(blogRepository.findBlogByUserIdFetchUser(userId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> categoryService.createMyCategory(userId, requestDto));

        assertEquals(BLOG_NOT_FOUND, exception.getErrorCode());
    }


    private CreateCategoryRequestDto createTestRequest(String name, String description, Long parentId) {
        return new CreateCategoryRequestDto(name, description, parentId);
    }

}