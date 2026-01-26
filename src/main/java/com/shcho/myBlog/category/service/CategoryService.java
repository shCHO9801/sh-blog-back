package com.shcho.myBlog.category.service;

import com.shcho.myBlog.blog.entity.Blog;
import com.shcho.myBlog.blog.repository.BlogRepository;
import com.shcho.myBlog.category.dto.CreateCategoryRequestDto;
import com.shcho.myBlog.category.entity.Category;
import com.shcho.myBlog.category.repository.CategoryRepository;
import com.shcho.myBlog.libs.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.shcho.myBlog.libs.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BlogRepository blogRepository;

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
            if ("미분류".equals(parent.getName())) {
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
}
