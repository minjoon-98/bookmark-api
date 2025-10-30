package io.github.minjoon98.bookmark.repository;

import io.github.minjoon98.bookmark.entity.Bookmark;
import io.github.minjoon98.bookmark.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    // 사용자별 전체 목록
    Page<Bookmark> findByUser(User user, Pageable pageable);

    // 사용자별 검색 (제목 또는 URL)
    Page<Bookmark> findByUserAndTitleContainingIgnoreCaseOrUserAndUrlContainingIgnoreCase(
            User user1, String title, User user2, String url, Pageable pageable);

    // 사용자별 태그 조회 (중복 제거)
    @Query("SELECT DISTINCT b FROM Bookmark b " +
           "JOIN b.bookmarkTags bt " +
           "JOIN bt.tag t " +
           "WHERE b.user = :user AND LOWER(t.name) = LOWER(:tagName)")
    Page<Bookmark> findDistinctByUserAndTagName(@Param("user") User user, @Param("tagName") String tagName, Pageable pageable);
}
