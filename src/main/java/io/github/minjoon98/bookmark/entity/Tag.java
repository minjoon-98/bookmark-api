package io.github.minjoon98.bookmark.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소문자 기준 유니크. 표시는 원문 그대로 해도 되지만, 간단히 소문자로 저장.
    @Column(nullable = false, length = 50)
    private String name; // normalize된 소문자

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookmarkTag> bookmarkTags = new ArrayList<>();

    @Builder
    public Tag(String name) {
        this.name = normalize(name);
    }

    public static String normalize(String raw) {
        return raw == null ? null : raw.trim().toLowerCase();
    }
}
