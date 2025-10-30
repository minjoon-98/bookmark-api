# API Specification — Bookmark Service

## 1. 개요

개인 북마크(즐겨찾기)와 태그를 관리하는 REST API입니다.
이 API는 사용자 단위로 북마크가 격리되며, 모든 북마크/태그 작업은 로그인한 사용자 범위 안에서만 수행됩니다.

## 2. 기본 정보

* **Base URL**: `http://localhost:8080`
* **Content-Type**: `application/json; charset=UTF-8`
* **인증 방식**: JWT Bearer Token

  * 로그인 성공 시 토큰을 발급받고 이후 요청 헤더에 아래처럼 추가합니다.

    ```http
    Authorization: Bearer <ACCESS_TOKEN>
    ```

## 3. 공통 규칙

### 3.1 페이징 & 정렬

모든 목록 조회 API에서 다음 파라미터를 지원합니다.

* `page`: 0-base 인덱스 (기본값: 0)
* `size`: 페이지 크기 (기본값: 20)
* `sort`: `field,direction` 형식 (예: `createdAt,desc`)

  * 다중 사용 가능
    예: `sort=createdAt,desc&sort=title,asc`
  * 허용 필드:

    * 북마크: `createdAt`, `updatedAt`, `title`, `url`

### 3.2 오류 응답 (공통 형식)

모든 에러는 전역 예외 처리기(`GlobalExceptionHandler`)에서 아래 형태로 내려갑니다.

```json
{
  "message": "에러 메시지",
  "status": 400,
  "timestamp": "2025-10-30T10:50:00",
  "errors": [
    { "field": "title", "message": "제목은 필수입니다" }
  ]
}
```

예)

* 400 Bad Request: 요청 유효성 실패(Validation)
* 401 Unauthorized: 토큰 없음 / 로그인 실패
* 403 Forbidden: 다른 사용자의 북마크 접근 등 소유권 위반
* 404 Not Found: 리소스 없음
* 409 Conflict: 이메일 중복 등
* 500 Internal Server Error: 예기치 못한 서버 오류

## 4. 엔드포인트 요약

| 기능         | 메서드    | 엔드포인트                            | 인증 | 설명                    |
| ---------- | ------ | -------------------------------- | -- | --------------------- |
| 회원가입       | POST   | `/auth/signup`                   | ❌  | 이메일·비밀번호로 신규 사용자 등록   |
| 로그인        | POST   | `/auth/login`                    | ❌  | JWT 발급 (액세스 토큰 반환)    |
| 로그아웃       | POST   | `/auth/logout`                   | ✅  | 클라이언트 토큰 폐기 안내 메시지 반환 |
| 북마크 등록     | POST   | `/bookmarks`                     | ✅  | 새 북마크 생성              |
| 북마크 목록 조회  | GET    | `/bookmarks`                     | ✅  | 전체/검색/페이지네이션/정렬 조회    |
| 북마크 상세 조회  | GET    | `/bookmarks/{id}`                | ✅  | 단일 북마크 상세 조회 (소유자 제한) |
| 북마크 수정     | PUT    | `/bookmarks/{id}`                | ✅  | 타이틀, URL, 메모 수정       |
| 북마크 삭제     | DELETE | `/bookmarks/{id}`                | ✅  | 북마크 삭제                |
| 태그별 북마크 조회 | GET    | `/bookmarks/by-tag?name={tag}`   | ✅  | 특정 태그를 가진 북마크 목록 조회   |
| 태그 추가      | POST   | `/bookmarks/{id}/tags`           | ✅  | 북마크에 태그들 추가           |
| 태그 제거      | DELETE | `/bookmarks/{id}/tags/{tagName}` | ✅  | 북마크에서 특정 태그 제거        |

> ✅ 인증이 필요한 API는 반드시 `Authorization: Bearer <token>` 헤더가 있어야 하며, 서버는 토큰의 사용자 정보로 접근 권한을 검사합니다.
> ❌ 인증 불필요 API는 누구나 호출할 수 있습니다.

---

## 5. 인증 / 사용자 관리 API

### 5.1 회원가입 — `POST /auth/signup`

**Request Body**

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

* `email`

  * 형식: 이메일
  * 중복 불가 (중복 시 409 Conflict, `DuplicateEmailException`)
* `password`

  * 4자 이상 100자 이하
  * 서버에서 BCrypt로 암호화 저장

**Response — 201 Created**

```json
{
  "message": "회원가입이 완료되었습니다"
}
```

**409 Conflict (이메일 중복)**

```json
{
  "message": "이미 가입된 이메일입니다 (user@example.com)",
  "status": 409,
  "timestamp": "2025-10-30T11:00:00"
}
```

---

### 5.2 로그인 — `POST /auth/login`

**Request Body**

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response — 200 OK**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "user@example.com"
}
```

* `accessToken`: 이후 모든 보호된 API 호출 시 Authorization 헤더에 포함
* 비밀번호 불일치 또는 미존재 계정일 경우 401

**401 Unauthorized (로그인 실패)**

```json
{
  "message": "이메일 또는 비밀번호가 올바르지 않습니다",
  "status": 401,
  "timestamp": "2025-10-30T11:05:00"
}
```

---

### 5.3 로그아웃 — `POST /auth/logout`

**요청 헤더**

```http
Authorization: Bearer <ACCESS_TOKEN>
```

**Response — 200 OK**

```json
{
  "message": "로그아웃되었습니다. 클라이언트에서 토큰을 삭제해주세요."
}
```

> 서버는 JWT를 서버 세션에 저장하지 않는 stateless 구조라서, 실제 무효화는 클라이언트 측(토큰 파기)에서 진행하도록 안내합니다.

---

## 6. 북마크 API

### 6.1 북마크 생성 — `POST /bookmarks` (✅ 인증 필요)

**요청 헤더**

```http
Authorization: Bearer <ACCESS_TOKEN>
```

**Request Body**

```json
{
  "title": "Google",
  "url": "https://www.google.com",
  "memo": "검색 엔진"
}
```

**Response — 201 Created**

```json
{
  "id": 1,
  "title": "Google",
  "url": "https://www.google.com",
  "memo": "검색 엔진",
  "createdAt": "2025-10-30T10:30:00",
  "updatedAt": "2025-10-30T10:30:00",
  "tags": []
}
```

---

### 6.2 북마크 목록 조회 — `GET /bookmarks` (✅ 인증 필요)

**요청 예시들**

기본 조회:

```http
GET /bookmarks
Authorization: Bearer <ACCESS_TOKEN>
```

검색:

```http
GET /bookmarks?search=github
Authorization: Bearer <ACCESS_TOKEN>
```

* `search`: 제목 또는 URL 부분 일치(대소문자 무시)

페이지네이션:

```http
GET /bookmarks?page=0&size=10
Authorization: Bearer <ACCESS_TOKEN>
```

정렬:

```http
GET /bookmarks?sort=title,asc
GET /bookmarks?sort=createdAt,desc&sort=title,asc
Authorization: Bearer <ACCESS_TOKEN>
```

복합:

```http
GET /bookmarks?search=git&page=0&size=10&sort=createdAt,desc
Authorization: Bearer <ACCESS_TOKEN>
```

**Response — 200 OK**

```json
{
  "content": [
    {
      "id": 1,
      "title": "Google",
      "url": "https://www.google.com",
      "memo": "검색 엔진",
      "createdAt": "2025-10-30T10:30:00",
      "updatedAt": "2025-10-30T10:30:00",
      "tags": ["java","spring"]
    }
  ],
  "pageable": { "pageNumber": 0, "pageSize": 20 },
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

> 응답은 항상 **해당 사용자 본인의 북마크만** 포함합니다.
> 다른 사용자의 데이터는 조회 불가.

---

### 6.3 북마크 상세 조회 — `GET /bookmarks/{id}` (✅ 인증 필요)

**요청**

```http
GET /bookmarks/1
Authorization: Bearer <ACCESS_TOKEN>
```

**Response — 200 OK**

```json
{
  "id": 1,
  "title": "Google",
  "url": "https://www.google.com",
  "memo": "검색 엔진",
  "createdAt": "2025-10-30T10:30:00",
  "updatedAt": "2025-10-30T10:30:00",
  "tags": ["java","spring"]
}
```

**404 Not Found 예시**

```json
{
  "message": "북마크를 찾을 수 없습니다. ID: 999",
  "status": 404,
  "timestamp": "2025-10-30T10:45:00"
}
```

**403 Forbidden 예시 (소유권 위반)**

```json
{
  "message": "접근 권한이 없습니다",
  "status": 403,
  "timestamp": "2025-10-30T10:46:00"
}
```

---

### 6.4 북마크 수정 — `PUT /bookmarks/{id}` (✅ 인증 필요)

**요청**

```http
PUT /bookmarks/1
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json
```

**Request Body**

```json
{
  "title": "Google Search",
  "memo": "세계 최고의 검색 엔진"
}
```

* null/미포함 필드는 기존 값 유지 (partial update 스타일)

**Response — 200 OK**

```json
{
  "id": 1,
  "title": "Google Search",
  "url": "https://www.google.com",
  "memo": "세계 최고의 검색 엔진",
  "createdAt": "2025-10-30T10:30:00",
  "updatedAt": "2025-10-30T10:40:00",
  "tags": ["java","spring"]
}
```

---

### 6.5 북마크 삭제 — `DELETE /bookmarks/{id}` (✅ 인증 필요)

**요청**

```http
DELETE /bookmarks/1
Authorization: Bearer <ACCESS_TOKEN>
```

**Response — 200 OK**

```json
{
  "message": "북마크가 성공적으로 삭제되었습니다"
}
```

---

## 7. 태그 API

태그는 소문자로 정규화돼 저장됩니다.
예: `"Java"`를 추가해도 실제로는 `"java"`로 관리됩니다.
중복 태그 추가는 무시됩니다.
어떤 북마크에도 더 이상 사용되지 않는 태그는 자동으로 정리될 수 있습니다.

---

### 7.1 태그별 조회 — `GET /bookmarks/by-tag?name={tag}` (✅ 인증 필요)

**요청**

```http
GET /bookmarks/by-tag?name=spring&page=0&size=20
Authorization: Bearer <ACCESS_TOKEN>
```

**Response — 200 OK**

```json
{
  "content": [
    {
      "id": 1,
      "title": "Google",
      "url": "https://www.google.com",
      "memo": "검색 엔진",
      "createdAt": "2025-10-30T10:30:00",
      "updatedAt": "2025-10-30T10:30:00",
      "tags": ["java","spring"]
    }
  ],
  "pageable": { "pageNumber": 0, "pageSize": 20 },
  "totalElements": 1,
  "totalPages": 1
}
```

---

### 7.2 태그 추가 — `POST /bookmarks/{id}/tags` (✅ 인증 필요)

**요청**

```http
POST /bookmarks/1/tags
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json
```

**Request Body**

```json
{
  "names": ["spring", "Java"]
}
```

**Response — 200 OK**

```json
{
  "id": 1,
  "title": "Google",
  "url": "https://www.google.com",
  "memo": "검색 엔진",
  "createdAt": "2025-10-30T10:30:00",
  "updatedAt": "2025-10-30T10:30:00",
  "tags": ["java","spring"]
}
```

---

### 7.3 태그 제거 — `DELETE /bookmarks/{id}/tags/{tagName}` (✅ 인증 필요)

**요청**

```http
DELETE /bookmarks/1/tags/java
Authorization: Bearer <ACCESS_TOKEN>
```

**Response — 200 OK**

```json
{
  "id": 1,
  "title": "Google",
  "url": "https://www.google.com",
  "memo": "검색 엔진",
  "createdAt": "2025-10-30T10:30:00",
  "updatedAt": "2025-10-30T10:30:00",
  "tags": ["spring"]
}
```

---

## 8. 요약 (리뷰어용 포인트)

* 이 API는 **Stateless JWT 인증** 구조.
* `User` ↔ `Bookmark` 는 1:N 관계.
  → 모든 북마크/태그 조작은 “현재 로그인한 유저 소유의 북마크인지” 검증 후 처리.
* 태그는 `BookmarkTag` 중간 테이블로 Many-to-Many를 명시적으로 관리.
* 조회 계열 엔드포인트(`GET /bookmarks`, `/bookmarks/by-tag`)는 캐시되어 Caffeine으로 응답 속도를 최적화.
* `GlobalExceptionHandler`를 통해 모든 오류 응답은 일관된 JSON 포맷으로 내려감.
