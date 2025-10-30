package io.github.minjoon98.bookmark.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "bookmark_tags",
    uniqueConstraints = @UniqueConstraint(columnNames = {"bookmark_id", "tag_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BookmarkTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 북마크
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bookmark_id", nullable = false)
    private Bookmark bookmark;

    // 태그
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 연관관계 편의 메서드(양방향 세팅)
    public static BookmarkTag link(Bookmark bookmark, Tag tag) {
        BookmarkTag bt = BookmarkTag.builder()
            .bookmark(bookmark)
            .tag(tag)
            .build();
        bookmark.getBookmarkTags().add(bt);
        tag.getBookmarkTags().add(bt);
        return bt;
    }
}
