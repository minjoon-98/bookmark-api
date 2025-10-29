# 북마크 관리 REST API

개인이 자주 방문하는 웹사이트를 관리할 수 있는 북마크 관리 시스템입니다.
**태그 기능**을 통해 북마크를 분류하고, 태그별로 빠르게 조회할 수 있습니다.

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
│   ├── Bookmark.java
│   └── Tag.java
├── dto/                 # DTO 클래스
│   ├── request/
│   │   ├── BookmarkCreateRequest.java
│   │   ├── BookmarkUpdateRequest.java
│   │   └── TagUpsertRequest.java
│   └── response/
│       ├── BookmarkResponse.java
│       ├── ErrorResponse.java
│       └── MessageResponse.java
├── exception/           # 예외 처리
│   ├── BookmarkNotFoundException.java
│   └── GlobalExceptionHandler.java
├── repository/          # JPA Repository
│   ├── BookmarkRepository.java
│   └── TagRepository.java
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

> 📗 정적 API 명세는 [docs/api-spec.md](docs/api-spec.md) 문서를 참고하세요.


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


---

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

### 6. 캐싱 전략 (Caffeine 기반)

**이유:**
- 북마크 서비스는 읽기 비중이 매우 높아, 조회 속도 최적화가 필요함.
- 단건·목록·태그별 조회의 반복 요청을 줄이기 위해 Spring Cache 적용.

**구현:**
- `CaffeineCacheManager` 기반 로컬 인메모리 캐시.
- KeyGenerator 빈(`pageableKeyGenerator`, `searchKeyGenerator`, `tagSearchKeyGenerator`)을 통해 캐시 키 자동 생성.
- Look-aside 전략으로 캐시 미스 시 DB 조회 후 캐시 적재.
- 생성·수정·삭제 시 관련 캐시 자동 무효화.

> 📘 자세한 TTL, 캐시 정책, 키 설계 등은 [docs/cache-design.md](docs/cache-design.md) 참고


## 개선할 점

## 라이선스
본 프로젝트는 KRAFTON Intra Platform Team 백엔드 개발자 채용 과제로 작성되었습니다.
