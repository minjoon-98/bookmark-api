# 📦 Bookmark 서비스 캐싱 설계 개요

## 1️⃣ 캐싱 전략 개요

- **전략:** `Look-aside (lazy loading)` 패턴

  → 캐시에 요청한 데이터가 없을 때만 DB를 조회하고, 결과를 캐시에 적재

  → 캐시 적중(Cache Hit) 시 DB 접근 없이 바로 응답

- **구현체:** `Caffeine (Local In-Memory Cache)`

  → 단일 인스턴스 환경에서 네트워크 오버헤드 없는 초저지연 캐시


---

## 2️⃣ 캐시 적용 이유

Bookmark 서비스는 **읽기 비중이 매우 높은 서비스(Read-heavy)** 구조를 가지고 있음.

즉, 사용자는 북마크를 “조회”하는 경우가 대부분이며, “추가/수정/삭제”는 빈도가 적음.

따라서 “쓰기 시 무효화 + 읽기 시 캐싱” 모델이 가장 효율적이다.

| 구분 | 접근 빈도 | 캐싱 적합성 | 설명 |
| --- | --- | --- | --- |
| 단건 상세 조회 (`GET /bookmarks/{id}`) | 매우 높음 | ✅ 매우 높음 | 동일 북마크를 반복적으로 조회 |
| 전체 목록 (`GET /bookmarks`) | 높음 | ✅ 높음 | 홈화면에서 매번 로딩되는 주요 데이터 |
| 검색/태그별 조회 | 중간 | ⚙️ 조건부 | 키 폭발 가능성 있으므로 일부만 캐싱 |
| 생성/수정/삭제 | 낮음 | ❌ 낮음 | 데이터 변경 시 캐시 무효화 필요 |

---

## 3️⃣ 캐시 구조 및 설정 근거

| 캐시명 | 대상 | TTL | 최대 크기 | 이유 |
| --- | --- | --- | --- | --- |
| `bookmarkById` | 단건 조회 | 10분 | 5000 | 상세 페이지 반복 접근 최적화 |
| `bookmarksFirstPage` | 전체 목록 첫 페이지 | 60초 | 1000 | 홈화면 체감 속도 개선, 변동 적음 |
| `bookmarksSearch` | 검색 결과 초기 페이지 | 30초 | 1000 | 검색어 다양성 높아 TTL 짧게 설정 |
| `bookmarksByTag` | 태그별 목록 (page ≤ 2) | 60초 | 1000 | 인기 태그 재조회 시 성능 향상 |

---

## 4️⃣ 캐시 저장 동작 (`@Cacheable`)

### 📍 `getBookmarkById`

- 단건 상세 페이지는 **자주 반복 조회**되므로 항상 캐싱.
- TTL 10분 설정으로 불필요한 DB 접근 최소화.

### 📍 `getBookmarks`

- 검색어 없고 `page=0`인 경우만 캐싱

  → “홈화면 첫 페이지” 조회 성능 최적화 목적.

- `검색어 있고 page ≤ 2`인 경우 별도 `bookmarksSearch` 캐시로 관리.

### 📍 `getBookmarksByTag`

- 인기 태그에 대한 반복 조회 대비.
- `page ≤ 2`까지만 캐싱 → 무한 키 증가 방지.

---

## 5️⃣ 캐시 무효화 동작 (`@CacheEvict`)

| 작업 | 캐시 무효화 대상 | 이유 |
| --- | --- | --- |
| **createBookmark()** | `bookmarksFirstPage`, `bookmarksSearch`, `bookmarksByTag` | 새 북마크가 추가되면 목록 및 검색 결과가 모두 달라짐 |
| **updateBookmark()** | `bookmarkById`, `bookmarksFirstPage`, `bookmarksSearch`, `bookmarksByTag` | 제목/URL 변경 시 상세 및 목록 모두 변경 |
| **deleteBookmark()** | 동일 | 삭제된 항목이 캐시 목록에 남아있을 수 있음 |
| **addTags()/removeTag()** | 동일 | 태그별 목록과 단건 상세 데이터가 모두 변동 |

➡️ `allEntries=true`를 사용하는 이유

: 데이터 변경 시 캐시 정합성 문제(오래된 캐시 반환)를 방지하기 위한 **보수적 정책**.

부분 무효화보다 안전하며, 캐시가 자동으로 재생성되기 때문에 일관성을 보장함.

---

## 6️⃣ 캐시 키 설계 (`CacheKeyGenerator`)

Pageable 정보를 직렬화 가능한 문자열 키로 변환하여 캐시 충돌을 방지.

```java
public static String pageKey(Pageable pageable) {
    return pageable.getPageNumber() + "|" +
           pageable.getPageSize() + "|" +
           pageable.getSort().toString();
}
```

예시 키:

```
"0|20|createdAt: DESC"

```

→ 페이지, 사이즈, 정렬 조합이 다른 경우 모두 별도 캐시 키로 관리되어 안전함.

---

## 7️⃣ 설계 선택 근거 요약

| 구분 | 선택 | 이유 |
| --- | --- | --- |
| **CaffeineCache** | 로컬 인메모리 | 단일 서버 환경에서 초저지연 (<1ms) |
| **Look-aside 전략** | 조회 시 적재 | 단순하고 Spring Cache 추상화에 적합 |
| **TTL + maxSize 병행** | 메모리 관리 | 일정 주기 자동 만료 + 메모리 초과 방지 |
| **보수적 무효화(allEntries)** | 정합성 우선 | 일관성 깨짐보다 일시적 캐시 미스가 낫다 |
| **조건부 캐싱** | 효율 극대화 | 검색, 태그별 결과의 폭발 방지 |

---

## ✅ 결론

이 캐시 구조는 다음과 같은 특성을 갖는다:

- 💨 **읽기 성능 향상** (DB 접근 제거)
- 🔒 **정합성 유지** (쓰기 시 캐시 무효화)
- ⚖️ **균형 잡힌 TTL 설계** (자주 바뀌는 데이터는 짧게, 안정 데이터는 길게)
- 🔄 **확장 가능 구조** (RedisCacheManager로 쉽게 대체 가능)