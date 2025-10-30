package io.github.minjoon98.bookmark.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookmarks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(length = 1000)
    private String memo;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(
        mappedBy = "bookmark",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    private List<BookmarkTag> bookmarkTags = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public Bookmark(String title, String url, String memo, User user) {
        this.title = title;
        this.url = url;
        this.memo = memo;
        this.user = user;
    }

    public void update(String title, String url, String memo) {
        if (title != null) {
            this.title = title;
        }
        if (url != null) {
            this.url = url;
        }
        if (memo != null) {
            this.memo = memo;
        }
    }

    // 편의 메서드
    public void addTag(Tag tag) {
        // 중복 방지 로직은 너가 넣어도 되고, 나중에 서비스 레이어에서 체크해도 돼
        BookmarkTag.link(this, tag);
    }

    public void removeTag(Tag tag) {
        // this.bookmarkTags에서 해당 tag 가진 것만 제거
        bookmarkTags.removeIf(bt -> {
            if (bt.getTag().equals(tag)) {
                // 양방향 정리
                tag.getBookmarkTags().remove(bt);
                bt = null; // GC 후보
                return true;
            }
            return false;
        });
    }
}
