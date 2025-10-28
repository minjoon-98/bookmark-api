# 북마크 관리 REST API

개인이 자주 방문하는 웹사이트를 관리할 수 있는 북마크 관리 시스템입니다.

## 목차
- [기술 스택](#기술-스택)
- [프로젝트 구조](#프로젝트-구조)
- [빌드 및 실행 방법](#빌드-및-실행-방법)
- [API 명세](#api-명세)
- [테스트 실행](#테스트-실행)
- [주요 설계 이유](#주요-설계-이유)
- [개선할 점](#개선할-점)

## 기술 스택
- **Language**: Java 21
- **Framework**: Spring Boot 3.5.7
- **Database**: H2 (In-Memory)
- **Build Tool**: Gradle
- **Test**: JUnit 5, Mockito, MockMvc
- **API Documentation**: Swagger/OpenAPI (SpringDoc)
- **CI**: GitHub Actions
- **Libraries**:
  - Spring Data JPA
  - Lombok
  - Validation

## 프로젝트 구조

```
src/main/java/io/github/minjoon98/bookmark/
├── config/              # 설정 클래스
│   └── OpenApiConfig.java
├── controller/          # REST API 컨트롤러
│   └── BookmarkController.java
├── domain/              # 엔티티 클래스
│   └── Bookmark.java
├── dto/                 # DTO 클래스
│   ├── request/
│   │   ├── BookmarkCreateRequest.java
│   │   └── BookmarkUpdateRequest.java
│   └── response/
│       ├── BookmarkResponse.java
│       ├── ErrorResponse.java
│       └── MessageResponse.java
├── exception/           # 예외 처리
│   ├── BookmarkNotFoundException.java
│   └── GlobalExceptionHandler.java
├── repository/          # JPA Repository
│   └── BookmarkRepository.java
└── service/             # 비즈니스 로직
    ├── BookmarkService.java         # 서비스 인터페이스
    └── BookmarkServiceImpl.java     # 서비스 구현체
```

### 패키지 구성 설명
- **config**: Swagger/OpenAPI 설정 등 애플리케이션 설정
- **controller**: HTTP 요청/응답 처리, REST API 엔드포인트 정의
- **domain**: 데이터베이스 엔티티 정의
- **dto**: 요청/응답 데이터 전송 객체
- **exception**: 예외 클래스 및 전역 예외 핸들러
- **repository**: 데이터베이스 접근 계층
- **service**: 비즈니스 로직 처리

## 빌드 및 실행 방법

### 1. 사전 요구사항
- Java 21 이상
- Git

### 2. 프로젝트 클론
```bash
git clone <repository-url>
cd bookmark-api
```

### 3. 실행 방법

#### Gradle 사용 (권장)
```bash
# Windows
gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

#### JAR 빌드 후 실행
```bash
# 빌드
./gradlew build

# 실행
java -jar build/libs/bookmark-0.0.1-SNAPSHOT.jar
```

### 4. 실행 확인
애플리케이션이 정상적으로 실행되면 다음 포트에서 접속 가능합니다:
- **애플리케이션**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:bookmarkdb`
  - Username: `sa`
  - Password: (공백)

## API 명세

### Swagger를 통한 API 문서 확인
애플리케이션 실행 후 **http://localhost:8080/swagger-ui.html** 에서 전체 API 명세를 확인할 수 있습니다.

### 주요 엔드포인트

| 기능 | 메서드 | 엔드포인트 | 설명 |
|------|--------|-----------|------|
| 북마크 등록 | POST | `/bookmarks` | 새로운 북마크 추가 |
| 북마크 목록 조회 | GET | `/bookmarks` | 전체 북마크 조회 (검색, 페이지네이션, 정렬 지원) |
| 북마크 상세 조회 | GET | `/bookmarks/{id}` | 특정 북마크 상세 정보 조회 |
| 북마크 수정 | PUT | `/bookmarks/{id}` | 북마크 정보 수정 |
| 북마크 삭제 | DELETE | `/bookmarks/{id}` | 북마크 삭제 |

### 요청/응답 예시

#### 1. 북마크 생성 (POST /bookmarks)
**요청**
```json
{
  "title": "Google",
  "url": "https://www.google.com",
  "memo": "검색 엔진"
}
```

**응답 (201 Created)**
```json
{
  "id": 1,
  "title": "Google",
  "url": "https://www.google.com",
  "memo": "검색 엔진",
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T10:30:00"
}
```

#### 2. 북마크 목록 조회 (GET /bookmarks)

**기본 조회**
```http
GET /bookmarks
```

**응답 (200 OK)**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Google",
      "url": "https://www.google.com",
      "memo": "검색 엔진",
      "createdAt": "2025-01-15T10:30:00",
      "updatedAt": "2025-01-15T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true
}
```

**검색 기능**
```http
GET /bookmarks?q=github
```
- `q`: 검색 키워드 (제목 또는 URL에서 부분 일치 검색, 대소문자 무시)

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
GET /bookmarks?q=git&page=0&size=10&sort=createdAt,desc
```

#### 3. 북마크 수정 (PUT /bookmarks/1)
**요청**
```json
{
  "title": "Google Search",
  "memo": "세계 최고의 검색 엔진"
}
```

**응답 (200 OK)**
```json
{
  "id": 1,
  "title": "Google Search",
  "url": "https://www.google.com",
  "memo": "세계 최고의 검색 엔진",
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T10:40:00"
}
```

#### 4. 북마크 삭제 (DELETE /bookmarks/1)
**응답 (200 OK)**
```json
{
  "message": "북마크가 성공적으로 삭제되었습니다"
}
```

### 에러 응답

#### 404 Not Found
```json
{
  "message": "북마크를 찾을 수 없습니다. ID: 999",
  "status": 404,
  "timestamp": "2025-01-15T10:45:00"
}
```

#### 400 Bad Request (Validation Error)
```json
{
  "message": "입력값 검증에 실패했습니다",
  "status": 400,
  "timestamp": "2025-01-15T10:50:00",
  "errors": [
    {
      "field": "title",
      "message": "제목은 필수입니다"
    },
    {
      "field": "url",
      "message": "URL은 필수입니다"
    }
  ]
}
```

## 테스트 실행

### 전체 테스트 실행
```bash
./gradlew test
```

### 테스트 커버리지
- **Repository Layer**: JPA 쿼리 메서드 및 CRUD 테스트
- **Service Layer**: 비즈니스 로직 및 예외 처리 테스트 (Mockito)
- **Controller Layer**: REST API 엔드포인트 및 검증 테스트 (MockMvc)

### 테스트 결과 확인
```bash
# 테스트 리포트는 다음 경로에서 확인 가능
build/reports/tests/test/index.html
```

## 주요 설계 이유

### 1. H2 In-Memory Database 선택
**이유**:
- 별도의 데이터베이스 설치 없이 즉시 실행 가능
- 개발 및 테스트 환경에서 빠른 초기화와 재시작
- 과제 제출 시 실행 편의성 극대화

**장점**:
- 의존성 최소화로 누구나 쉽게 실행 가능
- 애플리케이션 재시작 시 자동으로 초기화되어 테스트에 용이

### 2. DTO 패턴 사용
**이유**:
- Entity와 API 응답의 분리를 통한 계층 간 결합도 감소
- API 스펙 변경 시 도메인 모델에 영향 최소화
- 민감한 정보 노출 방지 및 필요한 데이터만 전송

**구조**:
- `BookmarkCreateRequest`: 생성 시 필수 값 검증
- `BookmarkUpdateRequest`: 수정 시 선택적 필드 업데이트
- `BookmarkResponse`: 클라이언트에게 전달할 데이터만 포함

### 3. 인터페이스 기반 서비스 설계 (DIP)
**이유**:
- 의존성 역전 원칙(Dependency Inversion Principle) 적용
- Controller는 구체적인 구현체가 아닌 Service 인터페이스에 의존
- 테스트 용이성 향상 및 구현체 교체 시 유연성 확보
- 비즈니스 로직의 명확한 계약(Contract) 정의

**구조**:
- `BookmarkService`: 비즈니스 로직 인터페이스
- `BookmarkServiceImpl`: 실제 구현체

### 4. 단순하고 직관적인 REST API 응답 형식
**이유**:
- 성공 응답: 데이터를 직접 반환 (불필요한 래퍼 없음)
- 삭제 응답: `MessageResponse`로 명확한 메시지 전달
- 산업 표준(Mozilla, Toss 등)을 따른 직관적인 응답 설계

**응답 형식**:
- 생성/조회/수정: 데이터 직접 반환
- 삭제: `{"message": "..."}`
- 에러: `ErrorResponse` (메시지, 상태, 타임스탬프, 상세 오류)

### 5. @RestControllerAdvice를 통한 전역 예외 처리
**이유**:
- 모든 컨트롤러에서 일관된 에러 응답 형식 제공
- 중복 코드 제거 및 유지보수성 향상
- HTTP 상태 코드와 에러 메시지의 표준화

**처리 예외**:
- `BookmarkNotFoundException`: 404 응답
- `MethodArgumentNotValidException`: 400 응답 (필드별 검증 오류)
- `Exception`: 500 응답 (예상치 못한 서버 오류)

## 개선할 점

### 1. 태그 기능 미구현
**현재 상황**: 제목과 URL로만 검색 가능
**개선 방향**:
- 북마크에 여러 태그를 추가할 수 있는 Many-to-Many 관계 구현
- 태그별 필터링 및 복합 검색 기능 추가

```java
// 개선 예시
@Entity
public class Tag {
    @Id @GeneratedValue
    private Long id;
    private String name;

    @ManyToMany(mappedBy = "tags")
    private Set<Bookmark> bookmarks;
}
```

### 2. 캐싱 미구현
**현재 상황**: 매 요청마다 데이터베이스 조회
**개선 방향**:
- Spring Cache를 활용한 자주 조회되는 데이터 캐싱
- Redis 등 분산 캐시 도입 검토

```java
// 개선 예시
@Cacheable(value = "bookmarks", key = "#id")
public BookmarkResponse getBookmarkById(Long id) {
    // ...
}
```

### 3. 테스트 커버리지 확장
**현재 상황**: 주요 기능 중심의 단위/통합 테스트
**개선 방향**:
- Edge case 및 경계값 테스트 추가
- 동시성 테스트 (동시 수정/삭제 시나리오)
- E2E 테스트를 위한 `@SpringBootTest` 확장

## 라이선스
본 프로젝트는 KRAFTON Intra Platform Team 백엔드 개발자 채용 과제로 작성되었습니다.
