# API Specification — Bookmark Service

## 개요
북마크 관리 서비스의 REST API 스펙을 상세히 설명합니다.

## 기본 정보
- **Base URL**: `http://localhost:8080`
- **Content-Type**: `application/json; charset=UTF-8`
- **Auth**: 없음 (데모 목적)

## 공통 규칙

### 페이징 & 정렬
- `page`: 0-base (기본: 0)
- `size`: 페이지 크기 (기본: 20)
- `sort`: `field,direction` (예: `createdAt,desc`), 다중 가능

### 오류 응답 (공통)
```json
{
  "message": "에러 메시지",
  "status": 400,
  "timestamp": "2025-01-15T10:50:00",
  "errors": [
    {"field":"title","message":"제목은 필수입니다"}
  ]
}
```

### 주요 엔드포인트

| 기능         | 메서드    | 엔드포인트                            | 설명                            |
| ---------- | ------ | -------------------------------- | ----------------------------- |
| 북마크 등록     | POST   | `/bookmarks`                     | 새로운 북마크 추가                    |
| 북마크 목록 조회  | GET    | `/bookmarks`                     | 전체 조회 (검색 `search`, 페이지네이션, 정렬 지원) |
| 북마크 상세 조회  | GET    | `/bookmarks/{id}`                | 특정 북마크 상세                     |
| 북마크 수정     | PUT    | `/bookmarks/{id}`                | 북마크 정보 수정                     |
| 북마크 삭제     | DELETE | `/bookmarks/{id}`                | 북마크 삭제                        |
| 태그별 조회 | GET    | `/bookmarks/by-tag?name={tag}`   | 해당 태그 보유 북마크 페이지 조회           |
| 태그 추가  | POST   | `/bookmarks/{id}/tags`           | 북마크에 태그 목록 추가                 |
| 태그 제거  | DELETE | `/bookmarks/{id}/tags/{tagName}` | 북마크에서 태그 제거                   |

### 요청/응답 예시

<details>
<summary><b>1) 북마크 생성 — POST /bookmarks</b></summary>

**Request**

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
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T10:30:00",
  "tags": []
}
```

</details>

---

<details>
<summary><b>2) 북마크 목록 조회 — GET /bookmarks</b></summary>


**기본 조회**

```http
GET /bookmarks
```

**검색**
```http
GET /bookmarks?search=github
```
- `search`: 검색 키워드 (제목 또는 URL에서 부분 일치 검색, 대소문자 무시)

**페이지네이션**
```http
GET /bookmarks?page=0&size=10
```
- `page`: 페이지 번호 (0부터 시작, 기본값: 0)
- `size`: 페이지 크기 (기본값: 20)

**정렬**
```http
GET /bookmarks?sort=title,asc
GET /bookmarks?sort=createdAt,desc&sort=title,asc
```
- `sort`: 정렬 기준 필드와 방향 (형식: `필드명,방향`)
- 허용 필드: `createdAt`, `updatedAt`, `title`, `url`
- 방향: `asc` (오름차순) 또는 `desc` (내림차순)
- 기본값: `createdAt,desc` (최신순)
- 다중 정렬 가능

**복합 사용**
```http
GET /bookmarks?search=git&page=0&size=10&sort=createdAt,desc
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
      "createdAt": "2025-01-15T10:30:00",
      "updatedAt": "2025-01-15T10:30:00",
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

</details>

---

<details>
<summary><b>3) 북마크 상세 조회 — GET /bookmarks/{id}</b></summary>

**Request**

```http
GET /bookmarks/1
```

**Response — 200 OK**

```json
{
  "id": 1,
  "title": "Google",
  "url": "https://www.google.com",
  "memo": "검색 엔진",
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T10:30:00",
  "tags": ["java","spring"]
}
```

**404 예시**

```json
{
  "message": "북마크를 찾을 수 없습니다. ID: 999",
  "status": 404,
  "timestamp": "2025-01-15T10:45:00"
}
```

</details>

---

<details>
<summary><b>4) 북마크 수정 — PUT /bookmarks/{id}</b></summary>

**Request**

```http
PUT /bookmarks/1
```

```json
{
  "title": "Google Search",
  "memo": "세계 최고의 검색 엔진"
}
```

**Response — 200 OK**

```json
{
  "id": 1,
  "title": "Google Search",
  "url": "https://www.google.com",
  "memo": "세계 최고의 검색 엔진",
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T10:40:00",
  "tags": ["java","spring"]
}
```

</details>

---

<details>
<summary><b>5) 북마크 삭제 — DELETE /bookmarks/{id}</b></summary>

**Request**

```http
DELETE /bookmarks/1
```

**Response — 200 OK**

```json
{
  "message": "북마크가 성공적으로 삭제되었습니다"
}
```

</details>

---

<details>
<summary><b>6) 태그별 조회 — GET /bookmarks/by-tag?name={tag}</b></summary>

**Request**

```http
GET /bookmarks/by-tag?name=spring&page=0&size=20
```

**Response — 200 OK (Page)**

```json
{
  "content": [
    {
      "id": 1,
      "title": "Google",
      "url": "https://www.google.com",
      "memo": "검색 엔진",
      "createdAt": "2025-01-15T10:30:00",
      "updatedAt": "2025-01-15T10:30:00",
      "tags": ["java","spring"]
    }
  ],
  "pageable": { "pageNumber": 0, "pageSize": 20 },
  "totalElements": 1,
  "totalPages": 1
}
```

</details>

---

<details>
<summary><b>7) 태그 추가 — POST /bookmarks/{id}/tags</b></summary>

**Request**

```http
POST /bookmarks/1/tags
```

```json
{ "names": ["spring", "Java"] }
```

**Response — 200 OK**
(태그는 소문자로 정규화되어 저장/노출)

```json
{
  "id": 1,
  "title": "Google",
  "url": "https://www.google.com",
  "memo": "검색 엔진",
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T10:30:00",
  "tags": ["java","spring"]
}
```

</details>

---

<details>
<summary><b>8) 태그 제거 — DELETE /bookmarks/{id}/tags/{tagName}</b></summary>

**Request**

```http
DELETE /bookmarks/1/tags/java
```

**Response — 200 OK**
(제거 후 남은 태그 반환)

```json
{
  "id": 1,
  "title": "Google",
  "url": "https://www.google.com",
  "memo": "검색 엔진",
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T10:30:00",
  "tags": ["spring"]
}
```

</details>

---

### 태그 규칙

* 저장/검색 모두 **대소문자 무시**(소문자로 정규화).
* 동일 태그 **중복 추가는 무시**.
* 어떤 북마크에서도 사용되지 않게 된 태그는 **정리(삭제)**.

