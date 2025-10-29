package io.github.minjoon98.bookmark.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Caffeine 기반 캐시 매니저 구성
     *
     * <p>각 캐시별로 TTL과 최대 크기를 개별 설정하여 메모리 사용 최적화
     * <p>recordStats()로 캐시 통계 수집 활성화 (Micrometer 연동 가능)
     */
    @Bean
    public CacheManager cacheManager() {
        // 단건 조회: 긴 TTL, 큰 용량 (재조회 이득 큼)
        CaffeineCache bookmarkById = new CaffeineCache(
            "bookmarkById",
            Caffeine.newBuilder()
                .maximumSize(5_000)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .build()
        );

        // 전체 목록 첫 페이지: 중간 TTL (홈 화면 체감 성능 개선)
        CaffeineCache bookmarksFirstPage = new CaffeineCache(
            "bookmarksFirstPage",
            Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .recordStats()
                .build()
        );

        // 검색 결과: 짧은 TTL (변동성 높음, 키 폭발 방지)
        CaffeineCache bookmarksSearch = new CaffeineCache(
            "bookmarksSearch",
            Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .recordStats()
                .build()
        );

        // 태그별 조회: 중간 TTL (특정 태그 반복 조회 최적화)
        CaffeineCache bookmarksByTag = new CaffeineCache(
            "bookmarksByTag",
            Caffeine.newBuilder()
                .maximumSize(1_000)
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .recordStats()
                .build()
        );

        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(List.of(
            bookmarkById,
            bookmarksFirstPage,
            bookmarksSearch,
            bookmarksByTag
        ));

        return cacheManager;
    }
}
