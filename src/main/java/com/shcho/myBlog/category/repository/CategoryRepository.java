package com.shcho.myBlog.category.repository;

import com.shcho.myBlog.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByBlogIdAndParentIdAndName(Long blogId, Long parentId, String name);

    List<Category> findAllByBlogIdOrderByNameAsc(Long blogId);

    List<Category> findAllByBlogIdAndParentIsNullOrderByNameAsc(Long id);

    boolean existsByBlogIdAndParentIdAndNameAndIdNot(Long blogId, Long parentId, String name, Long id);

    List<Category> findAllByBlogIdAndParentId(Long blogId, Long parentId);

    Optional<Category> findByBlogIdAndName(Long blogId, String name);

    boolean existsByParent_Id(Long id);
}
