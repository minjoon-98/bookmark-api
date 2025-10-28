package io.github.minjoon98.bookmark.repository;

import io.github.minjoon98.bookmark.domain.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Page<Bookmark> findByTitleContainingIgnoreCaseOrUrlContainingIgnoreCase(String title, String url, Pageable pageable);
}
