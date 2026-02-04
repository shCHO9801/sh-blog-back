package com.shcho.myBlog.post.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shcho.myBlog.post.dto.PostThumbnailResponseDto;
import com.shcho.myBlog.post.entity.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.shcho.myBlog.blog.entity.QBlog.blog;
import static com.shcho.myBlog.category.entity.QCategory.category;
import static com.shcho.myBlog.post.entity.QPost.post;
import static com.shcho.myBlog.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Page<PostThumbnailResponseDto> findPostThumbnailsByNickname(String nickname, Pageable pageable) {

        BooleanExpression[] conditions = new BooleanExpression[]{
                nicknameEq(nickname),
                isPublicOnly()
        };

        List<PostThumbnailResponseDto> content = baseThumbnailQuery()
                .where(conditions)
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseCountQuery()
                .where(conditions);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    public Page<PostThumbnailResponseDto> findPostThumbnailsByNicknameAndKeyword(String nickname, String keyword, Pageable pageable) {

        BooleanExpression[] conditions = new BooleanExpression[]{
                nicknameEq(nickname),
                titleContains(keyword),
                isPublicOnly()
        };

        List<PostThumbnailResponseDto> content = baseThumbnailQuery()
                .where(conditions)
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseCountQuery()
                .where(conditions);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    public Optional<Post> findPostByNicknameAndPostId(String nickname, Long postId) {

        BooleanExpression[] conditions = new BooleanExpression[]{
                nicknameEq(nickname),
                post.id.eq(postId),
                isPublicOnly()
        };

        Post result = queryFactory
                .selectFrom(post)
                .join(post.blog, blog).fetchJoin()
                .join(blog.user, user).fetchJoin()
                .join(post.category, category).fetchJoin()
                .where(conditions)
                .fetchOne();

        return Optional.ofNullable(result);
    }

    public Page<PostThumbnailResponseDto> findPostThumbnailsByNicknameAndCategoryId(String nickname, Long categoryId, Pageable pageable) {

        BooleanExpression[] conditions = new BooleanExpression[]{
                nicknameEq(nickname),
                categoryIdEq(categoryId),
                categoryBlogEqPostBlog(),
                isPublicOnly()
        };

        List<PostThumbnailResponseDto> content = baseThumbnailQueryWithCategoryJoin()
                .where(conditions)
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = baseCountQueryWithCategoryJoin()
                .where(conditions);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression nicknameEq(String nickname) {
        return user.nickname.eq(nickname);
    }

    private BooleanExpression isPublicOnly() {
        return post.isPublic.isTrue();
    }

    private BooleanExpression titleContains(String keyword) {
        return post.title.contains(keyword);
    }

    private BooleanExpression categoryIdEq(Long categoryId) {
        return category.id.eq(categoryId);
    }

    private BooleanExpression categoryBlogEqPostBlog() {
        return category.blog.eq(post.blog);
    }

    private JPAQuery<PostThumbnailResponseDto> baseThumbnailQuery() {
        return queryFactory
                .select(Projections.constructor(
                        PostThumbnailResponseDto.class,
                        post.id,
                        post.title,
                        post.isPublic,
                        post.createdAt
                ))
                .from(post)
                .join(post.blog, blog)
                .join(blog.user, user);
    }

    private JPAQuery<Long> baseCountQuery() {
        return queryFactory
                .select(post.count())
                .from(post)
                .join(post.blog, blog)
                .join(blog.user, user);
    }

    private JPAQuery<PostThumbnailResponseDto> baseThumbnailQueryWithCategoryJoin() {
        return baseThumbnailQuery()
                .join(post.category, category);
    }

    private JPAQuery<Long> baseCountQueryWithCategoryJoin() {
        return baseCountQuery()
                .join(post.category, category);
    }
}
