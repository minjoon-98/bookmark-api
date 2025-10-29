package io.github.minjoon98.bookmark.service;

import io.github.minjoon98.bookmark.entity.Bookmark;
import io.github.minjoon98.bookmark.entity.Tag;
import io.github.minjoon98.bookmark.dto.request.TagUpsertRequest;
import io.github.minjoon98.bookmark.dto.response.BookmarkResponse;
import io.github.minjoon98.bookmark.repository.BookmarkRepository;
import io.github.minjoon98.bookmark.repository.TagRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
class BookmarkServiceTagTest {

    @Mock BookmarkRepository bookmarkRepository;
    @Mock TagRepository tagRepository;

    @InjectMocks BookmarkServiceImpl sut;

    @Test
    @DisplayName("태그 추가 시 대소문자 무시·정규화 및 중복 무시")
    void addTags_normalize_and_dedup() {
        Bookmark bm = Bookmark.builder().title("t").url("u").build();
        given(bookmarkRepository.findById(1L)).willReturn(Optional.of(bm));
        // "spring"은 신규, "java"는 존재
        Tag java = Tag.builder().name("java").build();
        given(tagRepository.findByNameIgnoreCase("spring")).willReturn(Optional.empty());
        given(tagRepository.save(any(Tag.class))).willAnswer(inv -> inv.getArgument(0));
        given(tagRepository.findByNameIgnoreCase("java")).willReturn(Optional.of(java));

        BookmarkResponse res = sut.addTags(1L, new TagUpsertRequest(List.of("Spring", "java", "JAVA")));

        assertThat(res.getTags()).containsExactlyInAnyOrder("java","spring");
        verify(tagRepository, times(1)).save(argThat(t -> t.getName().equals("spring")));
    }

    @Test
    @DisplayName("태그 제거 후 더 이상 사용처 없으면 태그 정리")
    void removeTag_cleanup_if_orphan() {
        Bookmark bm = Bookmark.builder().title("t").url("u").build();
        Tag tag = Tag.builder().name("orphan").build();
        bm.addTag(tag);

        given(bookmarkRepository.findById(1L)).willReturn(Optional.of(bm));
        given(tagRepository.findByNameIgnoreCase("orphan")).willReturn(Optional.of(tag));

        sut.removeTag(1L, "orphan");

        verify(tagRepository, times(1)).delete(tag);
    }

    @Test
    @DisplayName("존재하지 않는 태그 제거 시 예외")
    void removeTag_not_found() {
        Bookmark bm = Bookmark.builder().title("t").url("u").build();
        given(bookmarkRepository.findById(1L)).willReturn(Optional.of(bm));
        given(tagRepository.findByNameIgnoreCase("nope")).willReturn(Optional.empty());

        assertThatThrownBy(() -> sut.removeTag(1L, "nope"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("태그로 북마크를 조회할 수 있다 (대소문자 무시)")
    void getBookmarksByTag() {
        // given
        Tag tag = Tag.builder().name("spring").build();
        Bookmark bm1 = Bookmark.builder().title("Spring Guide").url("https://spring.io").build();
        Bookmark bm2 = Bookmark.builder().title("Spring Boot").url("https://spring.io/projects/spring-boot").build();
        bm1.addTag(tag);
        bm2.addTag(tag);

        Page<Bookmark> page = new PageImpl<>(Arrays.asList(bm1, bm2), PageRequest.of(0, 20), 2);
        given(bookmarkRepository.findDistinctByTags_NameIgnoreCase(eq("spring"), any(Pageable.class)))
            .willReturn(page);

        // when
        Page<BookmarkResponse> result = sut.getBookmarksByTag("SPRING", PageRequest.of(0, 20));

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        verify(bookmarkRepository, times(1))
            .findDistinctByTags_NameIgnoreCase(eq("spring"), any(Pageable.class));
    }

    @Test
    @DisplayName("빈 문자열 태그는 무시된다")
    void addTags_ignore_empty_strings() {
        // given
        Bookmark bm = Bookmark.builder().title("Test").url("https://test.com").build();
        given(bookmarkRepository.findById(1L)).willReturn(Optional.of(bm));

        Tag validTag = Tag.builder().name("valid").build();
        given(tagRepository.findByNameIgnoreCase("valid")).willReturn(Optional.of(validTag));

        // when
        BookmarkResponse res = sut.addTags(1L, new TagUpsertRequest(List.of("valid", "", "  ", "\t")));

        // then
        assertThat(res.getTags()).containsExactly("valid");
        verify(tagRepository, never()).findByNameIgnoreCase("");
        verify(tagRepository, never()).findByNameIgnoreCase("  ");
    }

    @Test
    @DisplayName("태그 제거 시 다른 북마크가 사용 중이면 태그는 삭제되지 않는다")
    void removeTag_keep_if_still_used() {
        // given
        Bookmark bm1 = Bookmark.builder().title("BM1").url("https://bm1.com").build();
        Bookmark bm2 = Bookmark.builder().title("BM2").url("https://bm2.com").build();
        Tag sharedTag = Tag.builder().name("shared").build();

        bm1.addTag(sharedTag);
        bm2.addTag(sharedTag);

        given(bookmarkRepository.findById(1L)).willReturn(Optional.of(bm1));
        given(tagRepository.findByNameIgnoreCase("shared")).willReturn(Optional.of(sharedTag));

        // when
        sut.removeTag(1L, "shared");

        // then - 태그가 여전히 bm2에서 사용되므로 삭제되지 않아야 함
        verify(tagRepository, never()).delete(sharedTag);
    }
}
