package com.dduru.gildongmu.comment.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> children = new ArrayList<>();

    @Builder
    public Comment(String content, User user, Post post, Comment parent) {
        this.content = content;
        this.user = user;
        this.post = post;
        if (parent != null) {
            this.parent = parent;
            parent.addChildComment(this);
        }
    }

    public static Comment createComment(String content, User user, Post post, Comment parent) {
        return Comment.builder()
                .content(content)
                .user(user)
                .post(post)
                .parent(parent)
                .build();
    }

    private void addChildComment(Comment child) {
        this.children.add(child);
    }
}
