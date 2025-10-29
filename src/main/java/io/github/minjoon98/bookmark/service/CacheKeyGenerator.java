package io.github.minjoon98.bookmark.service;

import org.springframework.data.domain.Pageable;

/**
 * 캐시 키 생성 유틸리티
 *
 * <p>Pageable 객체를 직렬화 가능한 캐시 키 문자열로 변환
 */
public class CacheKeyGenerator {

    /**
     * Pageable을 "페이지번호|크기|정렬" 형태의 키로 변환
     *
     * @param pageable 페이지 정보
     * @return 캐시 키 문자열
     */
    public static String pageKey(Pageable pageable) {
        return pageable.getPageNumber() + "|" +
               pageable.getPageSize() + "|" +
               pageable.getSort().toString();
    }
}
