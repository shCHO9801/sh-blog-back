package com.shcho.myBlog.category.service;

import com.shcho.myBlog.blog.entity.Blog;
import com.shcho.myBlog.blog.repository.BlogRepository;
import com.shcho.myBlog.category.dto.CategoryTreeResponseDto;
import com.shcho.myBlog.category.dto.CreateCategoryRequestDto;
import com.shcho.myBlog.category.entity.Category;
import com.shcho.myBlog.category.repository.CategoryRepository;
import com.shcho.myBlog.libs.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.shcho.myBlog.libs.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BlogRepository blogRepository;
    private static final String DEFAULT_CATEGORY_NAME = "미분류";

    @Transactional
    public Category createMyCategory(Long userId, CreateCategoryRequestDto requestDto) {
        Blog myBlog = blogRepository.findBlogByUserIdFetchUser(userId)
                .orElseThrow(() -> new CustomException(BLOG_NOT_FOUND));

        String name = requestDto.name().trim();
        String description = requestDto.description();

        Category parent = null;

        if (requestDto.parentId() != null) {
            parent = categoryRepository.findById(requestDto.parentId())
                    .orElseThrow(() -> new CustomException(CATEGORY_NOT_FOUND));

            if (!parent.getBlog().getId().equals(myBlog.getId())) {
                throw new CustomException(CATEGORY_FORBIDDEN);
            }

            // parent는 root 여야 함
            if (parent.getParent() != null) {
                throw new CustomException(CATEGORY_PARENT_DEPTH_EXCEEDED);
            }

            // 미분류 카테고리는 자식을 가질 수 없음
            if (DEFAULT_CATEGORY_NAME.equals(parent.getName())) {
                throw new CustomException(CATEGORY_CANNOT_HAVE_CHILDREN);
            }
        }

        Long parentId = (parent == null) ? null : parent.getId();

        if (categoryRepository.existsByBlogIdAndParentIdAndName(myBlog.getId(), parentId, name)) {
            throw new CustomException(DUPLICATED_CATEGORY_NAME);
        }

        Category newCategory = Category.of(myBlog, parent, name, description);
        return categoryRepository.save(newCategory);
    }

    public List<Category> getMyRootCategories(Long userId) {
        Blog myBlog = blogRepository.findBlogByUserIdFetchUser(userId)
                .orElseThrow(() -> new CustomException(BLOG_NOT_FOUND));

        return categoryRepository.findAllByBlogIdAndParentIsNullOrderByNameAsc(myBlog.getId());
    }

    public Category getMyCategoryByCategoryId(Long userId, Long categoryId) {
        Blog myBlog = blogRepository.findBlogByUserIdFetchUser(userId)
                .orElseThrow(() -> new CustomException(BLOG_NOT_FOUND));

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(CATEGORY_NOT_FOUND));

        if (!myBlog.getId().equals(category.getBlog().getId())) {
            throw new CustomException(CATEGORY_FORBIDDEN);
        }

        return category;
    }

    public List<CategoryTreeResponseDto> getMyCategoryTree(Long userId) {
        Blog myBlog = blogRepository.findBlogByUserIdFetchUser(userId)
                .orElseThrow(() -> new CustomException(BLOG_NOT_FOUND));

        return buildTree(myBlog.getId());
    }

    public List<CategoryTreeResponseDto> getCategoryTreeByNickname(String nickname) {
        Blog getBlogByNickname = blogRepository.findBlogByUserNicknameFetchUser(nickname)
                .orElseThrow(() -> new CustomException(BLOG_NOT_FOUND));

        return buildTree(getBlogByNickname.getId());
    }

    private List<CategoryTreeResponseDto> buildTree(Long blogId) {
        List<Category> categories = categoryRepository.findAllByBlogIdOrderByNameAsc(blogId);

        // 부모 : 자식 map 생성
        Map<Long, List<Category>> childrenMap = categories.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        // 부모 리스트 생성
        List<Category> roots = categories.stream()
                .filter(Category::isRoot)
                .toList();

        List<CategoryTreeResponseDto> result = new ArrayList<>();

        for (Category root : roots) {
            List<CategoryTreeResponseDto.CategoryChildDto> children =
                    childrenMap.getOrDefault(root.getId(), new ArrayList<>())
                            .stream()
                            .sorted(Comparator.comparing(Category::getName))
                            .map(CategoryTreeResponseDto::childOf)
                            .toList();

            result.add(CategoryTreeResponseDto.of(root, children));
        }

        return result;
    }
}
