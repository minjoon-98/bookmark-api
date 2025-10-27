package io.github.minjoon98.bookmark.domain;

import jakarta.persistence.*;
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

    @Builder
    public Bookmark(String title, String url, String memo) {
        this.title = title;
        this.url = url;
        this.memo = memo;
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
}
