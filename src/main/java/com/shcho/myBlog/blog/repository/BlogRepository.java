package com.shcho.myBlog.blog.repository;

import com.shcho.myBlog.blog.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRepository extends JpaRepository<Blog, Long> {
}
