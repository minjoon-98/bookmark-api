package io.github.minjoon98.bookmark.repository;

import io.github.minjoon98.bookmark.entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Page<Bookmark> findByTitleContainingIgnoreCaseOrUrlContainingIgnoreCase(String title, String url, Pageable pageable);

    // 태그별 조회 (중복 제거)
    Page<Bookmark> findDistinctByTags_NameIgnoreCase(String name, Pageable pageable);
}
