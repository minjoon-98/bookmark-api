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
bookmark-api/
├── .github/
│   └── workflows/
│       └── ci.yml                        # GitHub Actions CI 설정
├── src/main/java/io/github/minjoon98/bookmark/
│   ├── config/                           # 애플리케이션 설정 관련
│   │   ├── CacheConfig.java              # Caffeine 캐시 설정
│   │   ├── CacheKeyConfig.java           # 캐시 키 전략 (Pageable, 검색어 등)
│   │   ├── OpenApiConfig.java            # Swagger / OpenAPI 설정
│   │   ├── SecurityConfig.java           # Spring Security + JWT 설정
│   ├── controller/                       # REST API 컨트롤러 계층
│   │   ├── AuthController.java           # 회원가입 / 로그인 / 로그아웃 API
│   │   └── BookmarkController.java       # 북마크 CRUD 및 태그 기능 API
│   ├── docs/                             # API 문서용 클래스
│   │   ├── AuthApiDoc.java               # 인증 API 문서
│   │   └── BookmarkApiDoc.java           # 북마크 API 문서
│   ├── dto/                              # 계층 간 데이터 전달 객체 (DTO)
│   │   ├── request/                      # 클라이언트 요청 DTO
│   │   │   ├── SignUpRequest.java        # 회원가입 요청
│   │   │   ├── LoginRequest.java         # 로그인 요청
│   │   │   ├── BookmarkCreateRequest.java# 북마크 생성 요청
│   │   │   ├── BookmarkUpdateRequest.java# 북마크 수정 요청
│   │   │   └── TagUpsertRequest.java     # 태그 추가/수정 요청
│   │   └── response/                     # 서버 응답 DTO
│   │       ├── BookmarkResponse.java     # 북마크 응답
│   │       ├── MessageResponse.java      # 단순 메시지 응답
│   │       ├── ErrorResponse.java        # 에러 응답 (상태, 메시지 포함)
│   │       └── LoginResponse.java        # 로그인 응답 (JWT 토큰 포함)
│   ├── entity/                           # JPA 엔티티 (DB 매핑 클래스)
│   │   ├── User.java                     # 사용자 엔티티 (이메일, 비밀번호)
│   │   ├── Bookmark.java                 # 북마크 엔티티
│   │   ├── Tag.java                      # 태그 엔티티
│   │   └── BookmarkTag.java              # 북마크-태그 매핑 엔티티 (중간 테이블)
│   ├── exception/                        # 도메인별 커스텀 예외 정의
│   │   ├── BookmarkNotFoundException.java# 북마크 미존재 예외
│   │   ├── DuplicateEmailException.java  # 이메일 중복 예외
│   │   ├── InvalidCredentialsException.java # 로그인 자격 증명 오류
│   │   ├── UserNotFoundException.java    # 사용자 미존재 예외
│   │   ├── AuthExceptionConstant.java    # 인증 관련 예외 상수
│   │   └── BookmarkExceptionConstant.java# 북마크 관련 예외 상수
│   ├── global/exception/                 # 공통 예외 처리 계층
│   │   ├── BookmarkException.java        # 공통 예외 추상 클래스
│   │   └── GlobalExceptionHandler.java   # @RestControllerAdvice 전역 예외 처리기
│   ├── repository/                       # 데이터 접근 계층 (JPA Repository)
│   │   ├── UserRepository.java           # 사용자 CRUD 및 이메일 조회
│   │   ├── BookmarkRepository.java       # 북마크 CRUD 및 사용자 기반 조회
│   │   └── TagRepository.java            # 태그 CRUD 및 이름 기반 조회
│   ├── service/                          # 비즈니스 로직 계층
│   │   ├── AuthService.java              # 인증/인가 서비스
│   │   ├── BookmarkService.java          # 북마크 서비스 인터페이스
│   │   └── BookmarkServiceImpl.java      # 북마크 서비스 구현체
│   └── util/                             # 공통 유틸리티
│       ├── IssueTokenResolver.java       # JWT 토큰 발급/서명 처리
│       ├── JwtKeyHolder.java             # JWT 비밀키 관리
│       └── JwtDecoderProvider.java       # JWT 검증용 디코더 제공
└── src/test/java/io/github/minjoon98/bookmark/ # 테스트 코드
    ├── controller/                       # Controller 단위 테스트
    │   ├── AuthControllerTest.java
    │   └── BookmarkControllerTest.java
    ├── repository/                       # Repository 단위 테스트
    │   ├── UserRepositoryTest.java
    │   ├── TagRepositoryTest.java
    │   └── BookmarkRepositoryTest.java
    ├── service/                          # Service 단위 테스트
    │   ├── AuthServiceTest.java
    │   ├── BookmarkServiceTest.java
    │   ├── BookmarkServiceCacheTest.java
    │   └── BookmarkServiceTagTest.java
    └── BookmarkApplicationTests.java     # 전체 애플리케이션 통합 테스트
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

> 📗 정적 API 명세는 [api-spec.md](api-spec.md) 문서를 참고해주세요.

### 주요 엔드포인트

| 기능        | 메서드    | 엔드포인트                            | 인증 | 설명                |
| --------- | ------ | -------------------------------- | -- | ----------------- |
| 회원가입      | POST   | `/auth/signup`                   | ❌  | 이메일, 비밀번호로 사용자 등록 |
| 로그인       | POST   | `/auth/login`                    | ❌  | JWT 발급            |
| 로그아웃      | POST   | `/auth/logout`                   | ✅  | 클라이언트 토큰 폐기       |
| 북마크 등록    | POST   | `/bookmarks`                     | ✅  | 새 북마크 생성          |
| 북마크 목록 조회 | GET    | `/bookmarks`                     | ✅  | 전체/검색/페이지 조회      |
| 북마크 상세 조회 | GET    | `/bookmarks/{id}`                | ✅  | 단일 북마크 조회         |
| 북마크 수정    | PUT    | `/bookmarks/{id}`                | ✅  | 타이틀, URL, 메모 수정   |
| 북마크 삭제    | DELETE | `/bookmarks/{id}`                | ✅  | 북마크 삭제            |
| 태그 추가     | POST   | `/bookmarks/{id}/tags`           | ✅  | 태그 등록             |
| 태그 제거     | DELETE | `/bookmarks/{id}/tags/{tagName}` | ✅  | 태그 삭제             |
| 태그별 조회    | GET    | `/bookmarks/by-tag?name={tag}`   | ✅  | 태그 기반 조회          |

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

### 2. Stateless 인증 (JWT)

- 세션 대신 JWT를 통해 사용자 인증 상태 유지
- 서버 확장성 향상 및 RESTful 아키텍처 준수
- 토큰 서명 및 검증: JwtKeyHolder + IssueTokenResolver

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

- @RestControllerAdvice + ErrorResponse 표준화
- 검증 실패(400), 인증 오류(401), 권한 오류(403), 자원 없음(404)
- 예외 상수 분리(AuthExceptionConstant, BookmarkExceptionConstant)

### 6. 캐싱 전략 (Caffeine 기반)

**이유:**
- 북마크 서비스는 읽기 비중이 매우 높아, 조회 속도 최적화가 필요함.
- 단건·목록·태그별 조회의 반복 요청을 줄이기 위해 Spring Cache 적용.

**구현:**
- `CaffeineCacheManager` 기반 로컬 인메모리 캐시.
- KeyGenerator 빈(`pageableKeyGenerator`, `searchKeyGenerator`, `tagSearchKeyGenerator`)을 통해 캐시 키 자동 생성.
- Look-aside 전략으로 캐시 미스 시 DB 조회 후 캐시 적재.
- 생성·수정·삭제 시 관련 캐시 자동 무효화.

> 📘 자세한 TTL, 캐시 정책, 키 설계 등은 [cache-design.md](cache-design.md) 참고해주세요.


## AI 활용 내역

본 프로젝트는 AI를 효율적 학습·문서화 도구로 활용하였습니다.
핵심 로직, 인증 구조, 캐싱, 예외 처리는 직접 설계 및 구현했습니다.

| 목적             | 도구              | 활용 내용                                      |
| -------------- | --------------- | ------------------------------------------ |
| 개념 이해 및 오류 디버깅 | ChatGPT (GPT-5) | Spring Security, JWT 구조, Caffeine 캐시 설정 질의 |
| 문서 정리 및 초안     | ChatGPT (GPT-5) | README, API 문서, 예외 설명 초안 정리                |
| 코드 리팩토링 보조     | Claude Code     | 테스트 템플릿, DTO 변환, 주석 정리                     |

**검증 방식**

* 생성 코드 전량 테스트 통과 확인
* 인증·예외·캐시·비즈니스 로직 직접 검증
* 모든 코드 동작 원리 직접 설명 가능

자세한 **AI 활용 방안은 [ai-notes.md](ai-notes.md) 참고해주세요.

## 라이선스
본 프로젝트는 KRAFTON Intra Platform Team 백엔드 개발자 채용 과제로 작성되었습니다.
