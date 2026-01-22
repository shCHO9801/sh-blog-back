package com.shcho.myBlog.blog.service;

import com.shcho.myBlog.blog.entity.Blog;
import com.shcho.myBlog.blog.repository.BlogRepository;
import com.shcho.myBlog.libs.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.shcho.myBlog.libs.exception.ErrorCode.BLOG_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogRepository blogRepository;

    public Blog getMyBlogByUserId(Long userId) {
        return blogRepository.findBlogByUserIdFetchUser(userId)
                .orElseThrow(() -> new CustomException(BLOG_NOT_FOUND));
    }

    public Blog getUserBlogByNickname(String nickname) {
        return blogRepository.findBlogByUserNicknameFetchUser(nickname)
                .orElseThrow(() -> new CustomException(BLOG_NOT_FOUND));
    }
}
