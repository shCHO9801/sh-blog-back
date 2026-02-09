package com.shcho.myBlog.post.entity;

import com.shcho.myBlog.blog.entity.Blog;
import com.shcho.myBlog.category.entity.Category;
import com.shcho.myBlog.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;

    private boolean isPublic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public boolean isPublic() {
        return this.isPublic;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public static Post of(Blog blog, Category category, String title, String content, boolean isPublic) {
        return Post.builder()
                .title(title)
                .content(content)
                .blog(blog)
                .category(category)
                .isPublic(isPublic)
                .build();
    }
}
