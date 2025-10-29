package io.github.minjoon98.bookmark.repository;

import io.github.minjoon98.bookmark.domain.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Test
    @DisplayName("태그를 저장할 수 있다")
    void save() {
        // given
        Tag tag = Tag.builder().name("spring").build();

        // when
        Tag saved = tagRepository.save(tag);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("spring");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("대소문자를 무시하고 태그를 찾을 수 있다")
    void findByNameIgnoreCase() {
        // given
        Tag tag = Tag.builder().name("spring").build();
        tagRepository.save(tag);

        // when & then
        assertThat(tagRepository.findByNameIgnoreCase("spring")).isPresent();
        assertThat(tagRepository.findByNameIgnoreCase("Spring")).isPresent();
        assertThat(tagRepository.findByNameIgnoreCase("SPRING")).isPresent();
        assertThat(tagRepository.findByNameIgnoreCase("sPrInG")).isPresent();
    }

    @Test
    @DisplayName("존재하지 않는 태그는 빈 Optional을 반환한다")
    void findByNameIgnoreCase_notFound() {
        // when
        Optional<Tag> result = tagRepository.findByNameIgnoreCase("nonexistent");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("대소문자를 무시하고 태그 존재 여부를 확인할 수 있다")
    void existsByNameIgnoreCase() {
        // given
        Tag tag = Tag.builder().name("java").build();
        tagRepository.save(tag);

        // when & then
        assertThat(tagRepository.existsByNameIgnoreCase("java")).isTrue();
        assertThat(tagRepository.existsByNameIgnoreCase("Java")).isTrue();
        assertThat(tagRepository.existsByNameIgnoreCase("JAVA")).isTrue();
        assertThat(tagRepository.existsByNameIgnoreCase("python")).isFalse();
    }

    @Test
    @DisplayName("동일한 이름(소문자)의 태그는 중복 저장할 수 없다")
    void unique_constraint_same_lowercase() {
        // given
        Tag tag1 = Tag.builder().name("kotlin").build();
        tagRepository.save(tag1);
        tagRepository.flush();

        // when & then - 정확히 같은 이름으로 저장 시도
        Tag tag2 = Tag.builder().name("kotlin").build();
        assertThatThrownBy(() -> {
            tagRepository.save(tag2);
            tagRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Tag.normalize()로 정규화하면 대소문자가 다르더라도 같은 값이 된다")
    void normalize_converts_to_lowercase() {
        // when & then
        assertThat(Tag.normalize("Spring")).isEqualTo("spring");
        assertThat(Tag.normalize("JAVA")).isEqualTo("java");
        assertThat(Tag.normalize("  Kotlin  ")).isEqualTo("kotlin");
        assertThat(Tag.normalize("kotlin")).isEqualTo("kotlin");
    }

    @Test
    @DisplayName("normalize는 null과 빈 문자열을 처리한다")
    void normalize_handles_null_and_empty() {
        // when & then
        assertThat(Tag.normalize(null)).isNull();
        assertThat(Tag.normalize("")).isEqualTo("");
        assertThat(Tag.normalize("   ")).isEqualTo("");
    }

    @Test
    @DisplayName("태그 생성 시 자동으로 정규화된다")
    void tag_is_normalized_on_creation() {
        // when
        Tag tag = Tag.builder().name("Spring Boot").build();

        // then
        assertThat(tag.getName()).isEqualTo("spring boot");
    }

    @Test
    @DisplayName("여러 태그를 저장하고 조회할 수 있다")
    void save_and_find_multiple_tags() {
        // given
        Tag spring = Tag.builder().name("spring").build();
        Tag java = Tag.builder().name("java").build();
        Tag kotlin = Tag.builder().name("kotlin").build();

        tagRepository.save(spring);
        tagRepository.save(java);
        tagRepository.save(kotlin);

        // when
        long count = tagRepository.count();

        // then
        assertThat(count).isEqualTo(3);
        assertThat(tagRepository.findByNameIgnoreCase("spring")).isPresent();
        assertThat(tagRepository.findByNameIgnoreCase("JAVA")).isPresent();
        assertThat(tagRepository.findByNameIgnoreCase("Kotlin")).isPresent();
    }

    @Test
    @DisplayName("태그를 삭제할 수 있다")
    void delete() {
        // given
        Tag tag = Tag.builder().name("temporary").build();
        Tag saved = tagRepository.save(tag);

        // when
        tagRepository.delete(saved);

        // then
        assertThat(tagRepository.findById(saved.getId())).isEmpty();
        assertThat(tagRepository.findByNameIgnoreCase("temporary")).isEmpty();
    }
}
