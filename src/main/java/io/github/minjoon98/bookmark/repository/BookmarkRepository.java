package io.github.minjoon98.bookmark.repository;

import io.github.minjoon98.bookmark.domain.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    List<Bookmark> findByTitleContainingOrUrlContaining(String title, String url);
}
