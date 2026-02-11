package com.shcho.myBlog.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private Long postId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UploadType type;

    @Column(nullable = false, length = 1024)
    private String objectName;

    @Column(nullable = false, length = 2048)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UploadStatus status;

    public void attachToPost(Long postId) {
        this.postId = postId;
        this.status = UploadStatus.ATTACHED;
    }

    public void markDeleted() {
        this.status = UploadStatus.DELETED;
    }
}
