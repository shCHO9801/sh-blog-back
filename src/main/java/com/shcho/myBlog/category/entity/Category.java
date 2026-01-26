package com.shcho.myBlog.category.entity;

import com.shcho.myBlog.blog.entity.Blog;
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
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_category_blog_name", columnNames = {"blog_id", "name"})
        }
)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    public static Category of(Blog blog, Category parent, String name, String description) {
        return Category.builder()
                .blog(blog)
                .parent(parent)
                .name(name)
                .description(description)
                .build();
    }

    public boolean isRoot() {
        return parent == null;
    }

    public void updateCategory(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
