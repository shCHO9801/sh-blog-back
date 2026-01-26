package com.shcho.myBlog.category.repository;

import com.shcho.myBlog.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByBlogIdAndParentIdAndName(Long blogId, Long parentId, String name);
}
