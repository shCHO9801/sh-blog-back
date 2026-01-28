package com.shcho.myBlog.category.service;

import com.shcho.myBlog.blog.entity.Blog;
import com.shcho.myBlog.blog.repository.BlogRepository;
import com.shcho.myBlog.category.dto.CategoryTreeResponseDto;
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

import java.util.ArrayList;
import java.util.List;
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

    @Test
    @DisplayName("내 단일 카테고리 조회 성공")
    void getMyCategorySuccess() {
        // given
        Long userId = 1L;
        User user = User.builder().userId(userId).nickname("nickname").build();
        Blog blog = Blog.builder().id(1L).user(user).build();
        Category myCategory = Category.builder().id(1L).blog(blog).build();

        when(blogRepository.findBlogByUserIdFetchUser(userId))
                .thenReturn(Optional.of(blog));
        when(categoryRepository.findById(myCategory.getId()))
                .thenReturn(Optional.of(myCategory));

        // when
        Category myCategoryByCategoryId = categoryService.getMyCategoryByCategoryId(userId, myCategory.getId());

        // then
        assertNotNull(myCategoryByCategoryId);
        assertEquals(myCategory, myCategoryByCategoryId);
    }

    @Test
    @DisplayName("내 단일 카테고리 조회 실패 - 권한이 없는 카테고리")
    void getMyCategoryFailedForbidden() {
        // given
        User user = User.builder().userId(1L).nickname("nickname").build();
        User otherUser = User.builder().userId(2L).nickname("otherNickname").build();
        Blog blog = Blog.builder().id(1L).user(user).build();
        Blog otherBlog = Blog.builder().id(2L).user(otherUser).build();
        Category otherCategory = Category.builder().id(1L).blog(otherBlog).build();

        when(blogRepository.findBlogByUserIdFetchUser(user.getUserId()))
                .thenReturn(Optional.of(blog));

        when(categoryRepository.findById(otherCategory.getId()))
                .thenReturn(Optional.of(otherCategory));


        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> categoryService.getMyCategoryByCategoryId(user.getUserId(), otherCategory.getId()));

        assertEquals(CATEGORY_FORBIDDEN, exception.getErrorCode());
    }

    @Test
    @DisplayName("내 카테고리 트리 조회 성공")
    void getMyCategoryTreeSuccess() {
        // given
        Long userId = 1L;
        User user = User.builder().userId(userId).nickname("nickname").build();
        Blog blog = Blog.builder().id(1L).user(user).build();

        Category parent1 = Category.builder().id(1L).name("parent1").blog(blog).parent(null).build();
        Category parent2 = Category.builder().id(2L).name("parent2").blog(blog).parent(null).build();

        List<Category> categories = new ArrayList<>();
        categories.add(parent1);
        categories.add(parent2);

        // parent1 children (정렬 검증을 위해 일부러 역순으로 넣음)
        categories.add(Category.builder().id(3L).name("b").blog(blog).parent(parent1).build());
        categories.add(Category.builder().id(4L).name("a").blog(blog).parent(parent1).build());

        // parent2 child
        categories.add(Category.builder().id(5L).name("child3").blog(blog).parent(parent2).build());

        when(blogRepository.findBlogByUserIdFetchUser(user.getUserId()))
                .thenReturn(Optional.of(blog));
        when(categoryRepository.findAllByBlogIdOrderByNameAsc(blog.getId()))
                .thenReturn(categories);

        // when
        List<CategoryTreeResponseDto> myCategoryTree = categoryService.getMyCategoryTree(user.getUserId());

        // then
        assertNotNull(myCategoryTree);
        assertEquals(2, myCategoryTree.size());

        CategoryTreeResponseDto dtoParent1 = findRootByName(myCategoryTree, "parent1");
        CategoryTreeResponseDto dtoParent2 = findRootByName(myCategoryTree, "parent2");

        assertEquals(2, dtoParent1.children().size());
        assertEquals(1, dtoParent2.children().size());

        // parent1 children 정렬 검증: a, b 순서여야 함
        assertEquals("a", dtoParent1.children().get(0).name());
        assertEquals("b", dtoParent1.children().get(1).name());

        // parent2 child 검증
        assertEquals("child3", dtoParent2.children().get(0).name());
    }

    @Test
    @DisplayName("내 root 카테고리 조회 성공")
    void getMyRootCategoriesSuccess() {
        // given
        Long userId = 1L;
        User user = User.builder().userId(userId).nickname("nickname").build();
        Blog blog = Blog.builder().id(1L).user(user).build();

        Category root1 = Category.builder().id(1L).name("A").blog(blog).parent(null).build();
        Category root2 = Category.builder().id(2L).name("B").blog(blog).parent(null).build();

        List<Category> roots = List.of(root1, root2);

        when(blogRepository.findBlogByUserIdFetchUser(userId))
                .thenReturn(Optional.of(blog));
        when(categoryRepository.findAllByBlogIdAndParentIsNullOrderByNameAsc(blog.getId()))
                .thenReturn(roots);

        // when
        List<Category> result = categoryService.getMyRootCategories(userId);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("A", result.get(0).getName());
        assertEquals("B", result.get(1).getName());
    }

    @Test
    @DisplayName("닉네임으로 카테고리 트리 조회 성공")
    void getCategoryTreeByNicknameSuccess() {
        // given
        String nickname = "nickname";

        User user = User.builder().userId(1L).nickname(nickname).build();
        Blog blog = Blog.builder().id(10L).user(user).build();

        Category parent1 = Category.builder().id(1L).name("parent1").blog(blog).parent(null).build();
        Category parent2 = Category.builder().id(2L).name("parent2").blog(blog).parent(null).build();

        List<Category> categories = new ArrayList<>();
        categories.add(parent1);
        categories.add(parent2);

        // parent1 children (정렬 검증)
        categories.add(Category.builder().id(3L).name("b").blog(blog).parent(parent1).build());
        categories.add(Category.builder().id(4L).name("a").blog(blog).parent(parent1).build());

        // parent2 child
        categories.add(Category.builder().id(5L).name("child3").blog(blog).parent(parent2).build());

        when(blogRepository.findBlogByUserNicknameFetchUser(nickname))
                .thenReturn(Optional.of(blog));
        when(categoryRepository.findAllByBlogIdOrderByNameAsc(blog.getId()))
                .thenReturn(categories);

        // when
        List<CategoryTreeResponseDto> result = categoryService.getCategoryTreeByNickname(nickname);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());

        CategoryTreeResponseDto dtoParent1 = findRootByName(result, "parent1");
        assertEquals(2, dtoParent1.children().size());
        assertEquals("a", dtoParent1.children().get(0).name());
        assertEquals("b", dtoParent1.children().get(1).name());
    }

    private CreateCategoryRequestDto createTestRequest(String name, String description, Long parentId) {
        return new CreateCategoryRequestDto(name, description, parentId);
    }

    private CategoryTreeResponseDto findRootByName(List<CategoryTreeResponseDto> tree, String rootName) {
        return tree.stream()
                .filter(r -> rootName.equals(r.name()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("root not found: " + rootName));
    }
}