package com.shcho.myBlog.blog.repository;

import com.shcho.myBlog.blog.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    @Query("select b from Blog b join fetch b.user where b.user.userId = :userId")
    Optional<Blog> findBlogByUserIdFetchUser(@Param("userId") Long userId);

    @Query("select b from Blog b join fetch b.user where b.user.nickname = :nickname")
    Optional<Blog> findBlogByUserNicknameFetchUser(@Param("nickname") String nickname);
}
