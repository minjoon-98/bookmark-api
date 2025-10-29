package io.github.minjoon98.bookmark.admin;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/cache")
@RequiredArgsConstructor
public class CacheAdminController {

    private final CacheManager cacheManager;

    @PostMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAll() {
        Map<String, Object> cleared = new HashMap<>();
        for (String name : cacheManager.getCacheNames()) {
            org.springframework.cache.Cache c = cacheManager.getCache(name);
            if (c != null) c.clear();
            cleared.put(name, "cleared");
        }
        return ResponseEntity.ok(cleared);
    }

    // (선택) Caffeine 통계 노출 – CacheConfig에서 recordStats() 켜둔 상태 가정
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        Map<String, Object> stats = new HashMap<>();
        for (String name : cacheManager.getCacheNames()) {
            org.springframework.cache.Cache sc = cacheManager.getCache(name);
            if (sc instanceof CaffeineCache cc) {
                com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache = cc.getNativeCache();
                var s = nativeCache.stats();
                Map<String, Object> m = new HashMap<>();
                m.put("hitRate", s.hitRate());
                m.put("missRate", s.missRate());
                m.put("requestCount", s.requestCount());
                m.put("evictionCount", s.evictionCount());
                stats.put(name, m);
            }
        }
        return ResponseEntity.ok(stats);
    }
}
