# Claude Code - Bookmark API Project Context

이 문서는 Claude Code가 프로젝트를 효과적으로 이해하고 작업할 수 있도록 작성된 컨텍스트 문서입니다.

## 프로젝트 개요

**KRAFTON Intra Platform Team 신입 백엔드 개발자 채용 과제**

개인이 자주 방문하는 웹사이트를 관리할 수 있는 북마크 관리 REST API 서버입니다.

### 과제 목표
- 코드 구조화 역량
- 테스트 작성 역량
- 유지보수성
- API 설계 감각
- AI 활용 역량 평가

## 기술 스택

- **Language**: Java 21
- **Framework**: Spring Boot 3.5.7
- **Database**: H2 In-Memory Database
- **Build Tool**: Gradle
- **Test Framework**: JUnit 5, Mockito, MockMvc
- **API Documentation**: Swagger/OpenAPI (SpringDoc)
- **Cache**: Caffeine (Local In-Memory)
- **Libraries**:
  - Spring Data JPA
  - Lombok
  - Jakarta Validation

## 현재 구현 완료된 기능

### 필수 기능 ✅
- [x] 북마크 CRUD (생성, 조회, 수정, 삭제)
- [x] 테스트 코드 (JUnit + Mockito)
- [x] API 명세 (Swagger UI + api-spec.md)
- [x] H2 In-Memory DB 사용
- [x] README.md 작성

### 선택 기능 (가산점) ✅
- [x] 태그 기능 (북마크에 태그 추가/제거/조회)
- [x] 검색 기능 (제목, URL 키워드 검색)
- [x] 페이지네이션 (목록 조회 시 페이지 단위 응답)
- [x] 전역 예외 처리 (@ControllerAdvice)
- [x] 캐싱 (Caffeine Cache)
- [x] **CI (GitHub Actions)** - JUnit 테스트 자동 실행 및 빌드 검증
- [ ] **인증 기능 (Auth-Lite)** - 🚧 현재 구현 중

## 프로젝트 구조

```
bookmark-api/
├── .github/
│   └── workflows/
│       └── ci.yml               # GitHub Actions CI 설정
├── src/main/java/io/github/minjoon98/bookmark/
│   ├── admin/                   # 관리자 기능 (캐시 관리 등)
│   │   └── CacheAdminController.java
│   ├── config/                  # 설정 클래스
│   │   ├── CacheConfig.java     # Caffeine 캐시 설정
│   │   ├── CacheKeyConfig.java  # 캐시 키 생성 로직
│   │   └── OpenApiConfig.java   # Swagger/OpenAPI 설정
│   ├── controller/              # REST API 컨트롤러
│   │   └── BookmarkController.java
│   ├── dto/                     # 데이터 전송 객체
│   │   ├── request/
│   │   │   ├── BookmarkCreateRequest.java
│   │   │   ├── BookmarkUpdateRequest.java
│   │   │   └── TagUpsertRequest.java
│   │   └── response/
│   │       ├── BookmarkResponse.java
│   │       ├── ErrorResponse.java
│   │       └── MessageResponse.java
│   ├── entity/                  # JPA 엔티티
│   │   ├── Bookmark.java        # 북마크 엔티티
│   │   └── Tag.java             # 태그 엔티티 (Many-to-Many)
│   ├── exception/               # 예외 클래스
│   │   ├── BookmarkNotFoundException.java
│   │   └── GlobalExceptionHandler.java  # @RestControllerAdvice
│   ├── repository/              # JPA Repository
│   │   ├── BookmarkRepository.java
│   │   └── TagRepository.java
│   └── service/                 # 비즈니스 로직
│       ├── BookmarkService.java         # 서비스 인터페이스
│       └── BookmarkServiceImpl.java     # 서비스 구현체
├── build.gradle                 # Gradle 빌드 설정
└── README.md                    # 프로젝트 문서
```

## 핵심 엔티티 설명

### 1. Bookmark (북마크)
```java
@Entity
@Table(name = "bookmarks")
public class Bookmark {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(length = 1000)
    private String memo;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(name = "bookmark_tags", ...)
    private Set<Tag> tags = new LinkedHashSet<>();
}
```

**특징:**
- Hibernate의 `@CreationTimestamp`, `@UpdateTimestamp` 사용
- Tags와 Many-to-Many 양방향 관계
- Builder 패턴 사용
- 업데이트 메서드에서 null 체크 후 선택적 필드 업데이트

### 2. Tag (태그)
```java
@Entity
@Table(name = "tags", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Tag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;  // 소문자로 정규화되어 저장

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToMany(mappedBy = "tags")
    private Set<Bookmark> bookmarks = new HashSet<>();
}
```

**특징:**
- 태그명은 소문자로 정규화 (`normalize()` 메서드)
- 대소문자 구분 없이 유니크 제약 (name 컬럼)
- 어떤 북마크에서도 사용되지 않으면 자동 삭제 로직 존재

## 아키텍처 원칙

### 1. 계층 분리 (Layered Architecture)
```
Controller (HTTP)
    ↓
Service (비즈니스 로직)
    ↓
Repository (데이터 접근)
    ↓
Entity (도메인 모델)
```

### 2. DTO 패턴
- **Request DTO**: 클라이언트 → 서버 데이터 전송
- **Response DTO**: 서버 → 클라이언트 데이터 전송
- Entity를 직접 노출하지 않음 (보안, 유연성)

### 3. 의존성 역전 원칙 (DIP)
- `BookmarkService` 인터페이스와 `BookmarkServiceImpl` 구현체 분리
- Controller는 인터페이스에만 의존
- 테스트 용이성 및 확장성 향상

### 4. 전역 예외 처리
- `@RestControllerAdvice`를 통해 일관된 에러 응답
- `ErrorResponse` 형태로 표준화된 에러 메시지 반환

### 5. 캐싱 전략
- **Look-aside (Lazy Loading)** 패턴
- Caffeine 로컬 인메모리 캐시
- 조회 시 캐싱, 변경 시 무효화 (`@CacheEvict`)
- 자세한 내용은 `cache-design.md` 참고

## 코딩 컨벤션

### 네이밍
- **Entity**: 단수형 (`Bookmark`, `Tag`)
- **Table**: 복수형 (`bookmarks`, `tags`)
- **DTO**: 용도 명시 (`BookmarkCreateRequest`, `BookmarkResponse`)
- **Service**: 인터페이스(`BookmarkService`) + 구현체(`BookmarkServiceImpl`)

### 패키지 구조
- 기능별 패키지 분리 (controller, service, repository, dto, entity, exception, config)
- DTO는 request/response로 세분화

### Lombok 사용
- `@Getter`: 필드 getter 자동 생성
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)`: 기본 생성자를 protected로
- `@Builder`: 빌더 패턴

### Validation
- Jakarta Validation 사용 (`@NotBlank`, `@Size` 등)
- `@Valid`로 요청 DTO 검증
- 검증 실패 시 `MethodArgumentNotValidException` → `GlobalExceptionHandler`에서 처리

## 테스트 전략

### 1. Repository Layer
- `@DataJpaTest` 사용
- JPA 쿼리 메서드 정상 동작 확인
- 예: `BookmarkRepositoryTest`, `TagRepositoryTest`

### 2. Service Layer
- `@ExtendWith(MockitoExtension.class)` 사용
- Repository를 Mock으로 대체
- 비즈니스 로직 및 예외 처리 검증
- 예: `BookmarkServiceTest`, `BookmarkServiceTagTest`, `BookmarkServiceCacheTest`

### 3. Controller Layer
- `@WebMvcTest` + `MockMvc` 사용
- HTTP 요청/응답 및 Validation 검증
- 예: `BookmarkControllerTest`

### 테스트 실행
```bash
./gradlew test
```

## CI/CD (GitHub Actions)

### 자동화 워크플로우
- **Workflow 파일**: [.github/workflows/ci.yml](.github/workflows/ci.yml)
- **트리거**: `main`, `develop` 브랜치로 push 또는 PR 생성 시

### CI 파이프라인 단계
1. **Checkout**: 코드 체크아웃
2. **Gradle Wrapper 검증**: 보안을 위한 Wrapper 무결성 확인
3. **JDK 21 설정**: Temurin 배포판 + Gradle 캐시
4. **테스트 실행**: JUnit 테스트 실행 (`./gradlew test`)
5. **테스트 리포트 생성**: `dorny/test-reporter`를 통한 체크런 생성
6. **빌드**: JAR 파일 생성 (`./gradlew bootJar`)
7. **아티팩트 업로드**: 빌드된 JAR 및 테스트 XML 업로드 (7일 보관)

### 주요 특징
- 테스트 실패 시에도 리포트 생성 (`continue-on-error: true`)
- 테스트 결과를 GitHub Checks로 시각화
- 빌드 성공 시 JAR 파일 자동 보관
- 테스트 XML을 아티팩트로 보관하여 추후 분석 가능

## API 엔드포인트

### 북마크 API
| 메서드 | 엔드포인트 | 설명 |
|--------|-----------|------|
| POST | `/bookmarks` | 북마크 생성 |
| GET | `/bookmarks` | 북마크 목록 조회 (검색, 페이지네이션, 정렬 지원) |
| GET | `/bookmarks/{id}` | 북마크 상세 조회 |
| PUT | `/bookmarks/{id}` | 북마크 수정 |
| DELETE | `/bookmarks/{id}` | 북마크 삭제 |
| GET | `/bookmarks/by-tag?name={tag}` | 태그별 북마크 조회 |
| POST | `/bookmarks/{id}/tags` | 북마크에 태그 추가 |
| DELETE | `/bookmarks/{id}/tags/{tagName}` | 북마크에서 태그 제거 |

자세한 API 명세는 [api-spec.md](api-spec.md) 참고

### Swagger UI
- **URL**: http://localhost:8080/swagger-ui.html
- 실시간 API 테스트 가능

### H2 Console
- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:bookmarkdb`
- **Username**: `sa`
- **Password**: (공백)

## 다음 구현 사항: 인증 기능 (Auth-Lite)

### 요구사항 (과제 선택 사항)
- 회원가입 / 로그인 / 로그아웃 기능 추가
- 본인 계정의 북마크만 조회/수정/삭제 가능
- 토큰 또는 세션 기반 인증 (자유 선택)
- Refresh 토큰, 이메일 인증 등은 불필요

### 구현 계획
1. **User 엔티티 생성**
   - 필드: id, email, password (암호화), createdAt
   - Bookmark와 One-to-Many 관계 설정

2. **JWT 기반 인증**
   - Spring Security 사용
   - 로그인 시 JWT 토큰 발급
   - API 요청 시 토큰 검증

3. **AuthController 생성**
   - `POST /auth/signup`: 회원가입
   - `POST /auth/login`: 로그인 (토큰 발급)
   - `POST /auth/logout`: 로그아웃 (토큰 무효화)

4. **BookmarkController 수정**
   - 인증된 사용자만 접근 가능
   - 본인의 북마크만 CRUD 가능

5. **예외 처리 추가**
   - `UserNotFoundException`
   - `DuplicateEmailException`
   - `InvalidCredentialsException`
   - `UnauthorizedException`

6. **테스트 작성**
   - AuthService 단위 테스트
   - 인증/인가 통합 테스트

## 실행 방법

### 애플리케이션 실행
```bash
# Gradle 사용
./gradlew bootRun

# JAR 빌드 후 실행
./gradlew build
java -jar build/libs/bookmark-0.0.1-SNAPSHOT.jar
```

### 테스트 실행
```bash
./gradlew test
```

### 실행 확인
- 애플리케이션: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console

## 주요 설계 결정 이유

자세한 내용은 [README.md](README.md)의 "주요 설계 이유" 섹션 참고

1. **H2 In-Memory DB**: 별도 설치 없이 즉시 실행 가능
2. **DTO 패턴**: Entity와 API 응답 분리로 결합도 감소
3. **인터페이스 기반 서비스**: DIP 적용으로 테스트 용이성 및 유연성 확보
4. **직관적인 REST API 응답**: 불필요한 래퍼 없이 데이터 직접 반환
5. **@RestControllerAdvice 전역 예외 처리**: 일관된 에러 응답 형식
6. **Caffeine 캐시**: 읽기 비중 높은 서비스 최적화

## 참고 문서

- [README.md](README.md): 프로젝트 전체 개요
- [api-spec.md](api-spec.md): 상세 API 명세
- [cache-design.md](cache-design.md): 캐싱 전략 및 설계
- [과제 PDF](Intra_Platform_Team_Backend_과제.pdf): 원본 과제 요구사항

## 주의사항

### 과제 제출 요구사항
- **제출 기한**: 과제 수령 후 3일 이내
- **제출 파일**: README.md, api-spec.md (또는 Swagger), ai-notes.md
- **평가 관점**: 코드 구조, 가독성, 테스트 품질, API 명세, 선택 기능 완성도, AI 활용 투명성

### AI 활용 가이드라인
- ai-notes.md에 사용한 AI 도구, 주요 프롬프트, 생성 코드 범위, 수정/검증 내용 기록
- 면접 시 모든 코드에 대해 직접 설명할 수 있어야 함

## Claude Code 작업 가이드

### 코드 작성 시
1. 기존 코드 스타일 및 구조 유지
2. Lombok 적극 활용 (`@Getter`, `@Builder`, `@NoArgsConstructor`)
3. 인터페이스 기반 서비스 설계 유지
4. DTO 패턴 준수 (Entity 직접 노출 금지)
5. 예외 처리는 GlobalExceptionHandler에 추가

### 테스트 작성 시
1. Repository: `@DataJpaTest`
2. Service: `@ExtendWith(MockitoExtension.class)` + Mockito
3. Controller: `@WebMvcTest` + MockMvc
4. Given-When-Then 패턴 권장

### 문서 작성 시
1. API 명세 변경 시 api-spec.md 업데이트
2. 중요한 설계 변경 시 README.md 업데이트
3. 캐시 로직 변경 시 cache-design.md 업데이트

---

**Last Updated**: 2025-01-30
**Status**: 인증 기능 구현 중
