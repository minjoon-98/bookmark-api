package io.github.minjoon98.bookmark.config;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;

/**
 * 캐시 키 생성 전략 설정
 */
@Configuration
public class CacheKeyConfig {

    /**
     * Pageable 파라미터를 직렬화 가능한 캐시 키로 변환
     * 형식: "pageNumber|pageSize|sort"
     */
    @Bean("pageableKeyGenerator")
    public KeyGenerator pageableKeyGenerator() {
        return (target, method, params) -> {
            for (Object param : params) {
                if (param instanceof Pageable pageable) {
                    return pageable.getPageNumber() + "|" +
                           pageable.getPageSize() + "|" +
                           pageable.getSort().toString();
                }
            }
            return "no-pageable";
        };
    }

    /**
     * 검색어 + Pageable 조합 캐시 키 생성
     * 형식: "keyword|pageNumber|pageSize|sort"
     */
    @Bean("searchKeyGenerator")
    public KeyGenerator searchKeyGenerator() {
        return (target, method, params) -> {
            String keyword = null;
            Pageable pageable = null;

            for (Object param : params) {
                if (param instanceof String) {
                    keyword = (String) param;
                } else if (param instanceof Pageable) {
                    pageable = (Pageable) param;
                }
            }

            if (keyword != null && pageable != null) {
                return keyword + "|" + pageable.getPageNumber() + "|" +
                       pageable.getPageSize() + "|" + pageable.getSort().toString();
            }
            return "no-search-params";
        };
    }

    /**
     * 태그명 + Pageable 조합 캐시 키 생성
     * 형식: "tagName|pageNumber|pageSize|sort"
     */
    @Bean("tagSearchKeyGenerator")
    public KeyGenerator tagSearchKeyGenerator() {
        return (target, method, params) -> {
            String tagName = null;
            Pageable pageable = null;

            for (Object param : params) {
                if (param instanceof String) {
                    tagName = (String) param;
                } else if (param instanceof Pageable) {
                    pageable = (Pageable) param;
                }
            }

            if (tagName != null && pageable != null) {
                return tagName + "|" + pageable.getPageNumber() + "|" +
                       pageable.getPageSize() + "|" + pageable.getSort().toString();
            }
            return "no-tag-params";
        };
    }
}
