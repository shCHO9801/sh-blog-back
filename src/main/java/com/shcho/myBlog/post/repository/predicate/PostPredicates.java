package com.shcho.myBlog.post.repository.predicate;

import com.querydsl.core.types.dsl.BooleanExpression;

import static com.shcho.myBlog.category.entity.QCategory.category;
import static com.shcho.myBlog.post.entity.QPost.post;
import static com.shcho.myBlog.user.entity.QUser.user;

public final class PostPredicates {

    private PostPredicates() {}

    public static BooleanExpression nicknameEq(String nickname) {
        return user.nickname.eq(nickname);
    }

    public static BooleanExpression isPublicOnly() {
        return post.isPublic.isTrue();
    }

    public static BooleanExpression categoryIdEq(Long categoryId) {
        return category.id.eq(categoryId);
    }

    public static BooleanExpression categoryBlogEqPostBlog() {
        return category.blog.eq(post.blog);
    }

    public static BooleanExpression blogEqBlogId(Long blogId) {
        return post.blog.id.eq(blogId);
    }

    public static BooleanExpression postEqPostId(Long postId) {
        return post.id.eq(postId);
    }

    public static BooleanExpression keywordInTitleOrContent(String keyword) {
        return post.title.contains(keyword)
                .or(post.content.isNotNull().and(post.content.contains(keyword)));
    }

    public static BooleanExpression isPublicEq(Boolean isPublic) {
        return isPublic == null ? null : post.isPublic.eq(isPublic);
    }
}
