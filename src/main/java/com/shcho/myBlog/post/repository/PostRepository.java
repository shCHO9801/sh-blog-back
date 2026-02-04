package com.shcho.myBlog.post.repository;

import com.shcho.myBlog.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
