# Springboot Project

Spring Boot 학습 및 포트폴리오용 백엔드 프로젝트입니다.
회원 인증(JWT/OAuth2)과 게시판 도메인을 중심으로 DDD 구조로 설계 및 구현합니다.

## 기술 스택

| 구분 | 내용 |
|------|------|
| Language | Java 26 |
| Framework | Spring Boot 4.1.0 |
| Security | Spring Security, JWT, OAuth2 |
| Database | PostgreSQL 18 (운영), H2 (테스트) |
| ORM | Spring Data JPA, QueryDSL |
| Cache | Redis |
| Migration | Flyway (`spring-boot-starter-flyway`) |
| API 문서 | Swagger (springdoc-openapi) |
| Build Tool | Gradle |
| Test | JUnit5 |
| Infra | Docker, Docker Compose, GitHub Actions (CI/CD) |
| Frontend | HTML5, Vanilla JS, CSS (별도 프레임워크 없이 API 동작 검증용 화면) |
| IDE | IntelliJ IDEA Ultimate |
| OS | macOS (Apple Silicon, MacBook Pro M5) |

## 개발 순서

1. **회원가입 / 로그인** (Spring Security, JWT, Redis)
2. **OAuth2 소셜 로그인**
3. **게시판** (JPA, QueryDSL)
4. 테스트 코드 보강 (도메인 개발과 병행)
5. API 문서화 (Swagger)
6. Docker Compose / CI(GitHub Actions) 구성

## 아키텍처 설계 방향

### Domain-First (패키지 바이 피처)

계층(Layer)이 아닌 **도메인(member, auth, board)을 최상위 기준**으로 패키지를 나눕니다.
도메인별로 `presentation / application / domain / infrastructure`를 모두 포함시켜, 하나의 도메인 안에서 대부분의 변경이 끝나도록 구성합니다.

- 도메인 수가 늘어나도 코드가 한 폴더(예: `application`)에 뒤섞이지 않음
- 도메인별 응집도가 높아 추후 모듈 분리(멀티 모듈), 서비스 분리 시에도 유리
- "계층 중심" 구조보다 DDD 철학(도메인을 우선)에 더 부합

> 참고: 계층을 최상위로 두는 Layer-First(`application/queryservice`, `domain/model/entity` 등으로 전체 도메인을 한 계층 폴더에 묶는 방식)도 가능하지만, 도메인이 3개 이상으로 늘어나는 이번 프로젝트에서는 Domain-First가 더 적합하다고 판단해 채택했습니다.

### CQRS 스타일 적용

각 도메인의 `application` 계층 내부에서 **Command(변경)와 Query(조회)의 책임을 분리**합니다. (별도 DB 분리가 아닌, 서비스 레이어의 책임 분리 수준으로 적용)

- 회원가입 같은 Command는 검증/저장 로직에 집중
- 게시판 목록 조회 같은 Query는 QueryDSL 기반 동적 조건/페이징에 집중
- 우선 게시판(`board`) 도메인에 먼저 적용 후, 다른 도메인으로 확장 예정

### Mapper를 통한 presentation ↔ application 타입 분리

Controller는 `application` 계층의 Service에 presentation의 Request DTO를 직접 전달하지 않고, Service가 반환하는 결과도 presentation의 Response DTO 타입을 그대로 담지 않습니다. `presentation.mapper`의 Mapper가 양방향 변환을 담당합니다.

```
[요청]  Controller → Mapper.toCommand(Request DTO) → Command 객체 → Service
[응답]  Service → CommandResult(순수 값 객체) → Mapper.toResponse(CommandResult) → Response DTO → Controller
```

- Service는 presentation 레이어의 타입(Request/Response DTO)을 전혀 의존하지 않습니다. Command와 CommandResult 모두 `application.command.dto`에 위치한 순수 값 객체입니다.
- API 스펙(Request/Response DTO)이 바뀌어도 Mapper만 수정하면 되고, Service 내부 로직은 영향받지 않습니다.
- "회원가입을 수행하라"는 도메인 의도를 Command 객체 이름으로 명시적으로 드러냅니다. (예: `MemberRegisterCommand`)
- 예: `LoginCommandService.login()`은 `LoginCommandResult`(accessToken, refreshToken 등 순수 값)만 반환하고, 이를 클라이언트 응답 Body로 노출할 `LoginResponse`로 변환하는 책임은 `AuthMapper.toResponse()`가 맡습니다. (refreshToken은 Cookie로 별도 전달되므로 Response DTO에는 포함되지 않습니다.) `TokenReissueCommandService.reissue()` → `ReissueCommandResult` → `ReissueResponse`도 동일한 패턴입니다. `*CommandResult` 타입은 항상 application 내부 표현이며, Controller가 이를 그대로 클라이언트에 응답하지 않습니다.
- Query 쪽도 동일한 원칙을 적용할 수 있으나, 현재 Query는 단순 파라미터(예: username 하나) 수준이라 별도 Query 객체 없이 진행하고 있습니다.

### Command Service 분리 기준

하나의 Command Service가 여러 유스케이스를 떠안지 않도록, 클래스명을 좁고 구체적인 책임 단위로 짓습니다. (`MemberQueryService` → `MemberAvailabilityQueryService`로 좁힌 것과 같은 원칙)

- 호출 시점이나 부수효과가 다른 유스케이스는 별도 클래스로 분리합니다. 예: 로그인(`LoginCommandService` - 사용자 행위, 실패 카운트/이력 기록 등 부수효과 多)과 토큰 재발급(`TokenReissueCommandService` - 시스템 자동 트리거, 비교적 가벼움)을 하나의 `AuthCommandService`로 묶지 않고 분리했습니다.
- 반대로 `MemberRegisterCommandService`, `MemberEmailCommandService`처럼 도메인 내에서도 책임이 명확히 다른 기능은 처음부터 별도 클래스로 만듭니다.
- `Auth`, `Member`처럼 도메인명을 그대로 딴 포괄적인 이름(`AuthCommandService`, `MemberCommandService`)은 지양합니다.

### 의존성 역전 (DIP)

상위 레이어가 하위 레이어의 구체 구현을 직접 의존하지 않고, `domain.service`(또는 `global`)에 정의된 인터페이스에만 의존하도록 합니다. 실제 구현은 인터페이스를 통해 주입받습니다.

```
application (Service) → domain.service (인터페이스) ← infrastructure (구현체)
```

**1. application ↔ infrastructure**

- Repository가 이 패턴의 대표 예시입니다: `domain.repository`에 인터페이스(`MemberRepository`)를 두고, Spring Data JPA가 런타임에 구현체를 자동 생성합니다.
- Redis를 사용하는 부분도 동일하게 적용합니다:
  - `member.domain.service.EmailVerifier` (인터페이스) ← `member.infrastructure.redis.RedisEmailVerifier` (구현체)
  - `auth.domain.service.RefreshTokenManager` (인터페이스) ← `auth.infrastructure.redis.RedisRefreshTokenManager` (구현체)
- 구현체 클래스명은 `Redis{인터페이스명}` 형태로, 어떤 기술로 구현했는지 이름에 드러냅니다.
- `JwtProvider.java`, `AesGcmCryptoService.java`, `EmailHasher.java`는 이 패턴에서 제외하고 application이 직접 의존합니다. 셋 다 외부 저장소나 네트워크 호출 없이, 메모리 안에서 끝나는 순수 알고리즘 계산(JWT 조립/파싱, AES 암복호화, SHA-256 해시)이라 "교체 가능한 외부 자원"이라는 개념 자체가 약하기 때문입니다.
  - 판단 기준: 클래스가 외부 시스템(DB, Redis, 메일 서버 등)과 I/O를 하면 인터페이스로 분리하고, 그렇지 않고 입력을 받아 계산만 해서 반환하면(순수 함수에 가까우면) 직접 의존합니다.
  - 예: `RefreshTokenManager`는 Redis에 값을 저장/조회하므로 분리 대상이지만, 그 안에서 다루는 토큰 문자열 자체를 만들고 검증하는 `JwtProvider`는 순수 계산이라 분리 대상이 아닙니다.

**2. global ↔ 도메인**

`global`은 여러 도메인이 공통으로 의존하는 영역이므로, 특정 도메인의 구체 구현을 직접 알아서는 안 됩니다. (반대로 도메인이 `global`을 아는 것은 자연스러운 방향입니다.)

- `global.config.SecurityConfig`는 인증 필터가 필요하지만, 그 구현이 JWT라는 사실을 직접 알지 않습니다. `global.security.AuthenticationTokenFilterProvider`(인터페이스)에만 의존하고, 실제 구현(`auth.infrastructure.jwt.JwtAuthenticationFilterProvider`)이 `JwtAuthenticationFilter`를 생성해 제공합니다.
- OAuth2 로그인 설정도 동일한 패턴입니다. `SecurityConfig`는 `global.security.OAuthLoginProvider`(인터페이스)에만 의존하고, 실제 구현(`auth.infrastructure.oauth.GoogleOAuthLoginProvider`)이 구글 전용 `CustomOAuth2UserService`와 `OAuth2LoginSuccessHandler`를 묶어 제공합니다. 나중에 카카오 등 다른 제공자를 추가해도 `SecurityConfig`는 수정할 필요가 없습니다 (제공자별 Provider 구현체만 추가).
- 이렇게 하면 인증 방식이 JWT가 아닌 다른 방식으로 바뀌어도 `SecurityConfig`는 수정할 필요가 없습니다.

**적용하지 않는 범위**

- 헥사고날 아키텍처의 `port.in`/`port.out`처럼 Service 계층까지 인터페이스로 한 번 더 추상화하는 패턴은 적용하지 않습니다. (현재 프로젝트 규모에서는 Controller가 Service를 직접 호출하는 구조로 충분하다고 판단)

### Aggregate 설계 원칙

도메인 패키지(`member`, `board`, `auth`) 자체를 Aggregate 경계로 삼고, 각 도메인 안에서 하나의 엔티티를 **Aggregate Root**로 둡니다. 별도의 `aggregate` 패키지를 만들지 않고, 기존 `domain.model.entity` 패키지 안에서 Root 여부를 Javadoc 주석으로 명시하는 방식을 사용합니다.

| Aggregate | Root | 포함 요소 | 불변식 예시 |
|---|---|---|---|
| Member | `Member.java` | Email(VO), Password(VO) | 이메일 인증을 통과한 이메일만 저장 가능, 로그인 실패 5회 시 잠김 |
| Board | `Board.java` | `Comment.java`, Title(VO), Content(VO) | 게시글 삭제 시 댓글도 함께 삭제, 작성자만 게시글/댓글 삭제 가능 |

> `MemberHistory.java`(로그인/정보변경 이력)는 Member와 연관되지만, "벌어진 사실의 기록"이라는 로그성 데이터 특성상 Member Aggregate에 강하게 종속시키지 않고 `MemberHistoryRepository`를 통해 직접 저장합니다. (Comment처럼 부모 없이는 의미가 없는 종속 관계와는 다름) `MemberOAuth.java`(소셜 로그인 연동 정보)도 동일하게 `MemberOAuthRepository`를 통해 직접 저장합니다.
>
> `auth` 도메인은 별도 JPA Aggregate를 두지 않습니다. Refresh Token은 DB가 아닌 Redis에만 저장하므로(`RefreshTokenManager.java` 인터페이스 + `RedisRefreshTokenManager.java` 구현체), Aggregate Root 개념이 적용되는 영속성 엔티티가 존재하지 않습니다.

**적용 규칙**
1. Repository는 **Aggregate Root 단위로만 생성**합니다. (`CommentRepository`는 별도로 두지 않고, `Comment.java`는 `BoardRepository`/`Board.java`를 통해서만 조회·생성·삭제)
2. 다른 Aggregate를 참조할 때는 **객체 참조 대신 ID 참조**를 사용합니다. (예: `Comment.java`는 작성자를 `Member.java` 객체가 아닌 `memberId`로 보관 → Aggregate 간 결합도를 낮춤)
3. 하나의 트랜잭션에서는 **하나의 Aggregate만 수정**하는 것을 원칙으로 합니다.

## 패키지 구조

```
src/main/java/com/hyunsu/springboot
 │
 ├─ member                          # 회원 도메인 (가입, 회원 정보) (Aggregate: Member)
 │   ├─ presentation
 │   │   ├─ controller
 │   │   ├─ mapper                   # MemberMapper.java (Request DTO -> Command 변환)
 │   │   └─ dto
 │   │       ├─ request
 │   │       └─ response
 │   ├─ application
 │   │   ├─ command
 │   │   │   ├─ dto                  # MemberRegisterCommand.java, EmailCodeVerifyCommand.java, OAuthLoginCommand.java, OAuthLoginCommandResult.java
 │   │   │   ├─ MemberRegisterCommandService.java
 │   │   │   ├─ MemberEmailCommandService.java
 │   │   │   └─ OAuthLoginCommandService.java   # 소셜 로그인 시 회원 조회/연동/신규가입 처리
 │   │   └─ query                   # MemberAvailabilityQueryService.java (아이디 등 중복확인)
 │   ├─ domain
 │   │   ├─ model
 │   │   │   ├─ entity                  # Member.java (Aggregate Root), MemberHistory.java, MemberOAuth.java
 │   │   │   └─ vo                      # Role, MemberStatus, MemberHistoryType, OAuthProvider 등 값 객체
 │   │   ├─ repository               # MemberRepository.java, MemberHistoryRepository.java, MemberOAuthRepository.java (인터페이스, Spring Data JPA가 구현)
 │   │   └─ service                  # EmailVerifier.java (인터페이스 - infrastructure 구현을 application이 직접 의존하지 않도록 추상화)
 │   └─ infrastructure
 │       ├─ repository                # Repository 구현체 (JPA, 현재는 Spring Data JPA 자동 구현이라 비어있음)
 │       └─ redis
 │           └─ RedisEmailVerifier.java   # EmailVerifier의 Redis 구현체 (이메일 인증코드 발급/검증)
 │
 ├─ board                           # 게시판 도메인 (Aggregate: Board)
 │   ├─ presentation
 │   │   ├─ controller               # 게시글, 댓글 Controller
 │   │   └─ dto
 │   ├─ application
 │   │   ├─ command                 # 게시글/댓글 작성·수정·삭제
 │   │   └─ query                   # 게시글 조회/검색 (QueryDSL), 댓글 목록 조회
 │   ├─ domain
 │   │   ├─ model
 │   │   │   ├─ entity
 │   │   │   │   ├─ Board.java           # Aggregate Root
 │   │   │   │   └─ Comment.java          # Board에 종속 (Board를 통해서만 생성/삭제)
 │   │   │   └─ vo
 │   │   ├─ repository               # BoardRepository만 존재 (CommentRepository 별도로 두지 않음)
 │   │   └─ service
 │   └─ infrastructure
 │       └─ repository
 │           └─ querydsl              # QueryDSL 커스텀 구현체
 │
 ├─ auth                            # 인증/인가 (로그인, 토큰, OAuth2)
 │   ├─ presentation
 │   │   ├─ controller               # AuthController.java (로그인, 토큰 재발급)
 │   │   ├─ mapper                   # AuthMapper.java (Request DTO <-> Command/CommandResult <-> Response DTO 양방향 변환)
 │   │   └─ dto
 │   │       ├─ request               # LoginRequest.java
 │   │       └─ response              # LoginResponse.java, ReissueResponse.java
 │   ├─ application
 │   │   ├─ command
 │   │   │   ├─ dto                  # LoginCommand.java, LoginCommandResult.java, ReissueCommandResult.java
 │   │   │   ├─ LoginCommandService.java          # 로그인 처리
 │   │   │   └─ TokenReissueCommandService.java    # Access Token 재발급 처리
 │   ├─ domain
 │   │   └─ service                  # RefreshTokenManager.java (인터페이스 - JPA 엔티티는 없지만, Redis 접근을 추상화)
 │   └─ infrastructure
 │       ├─ jwt
 │       │   ├─ JwtProvider.java                       # 토큰 생성/검증
 │       │   ├─ JwtAuthenticationFilter.java            # 매 요청마다 Authorization 헤더 검증
 │       │   └─ JwtAuthenticationFilterProvider.java     # global.security.AuthenticationTokenFilterProvider의 JWT 구현체
 │       ├─ redis
 │       │   └─ RedisRefreshTokenManager.java   # RefreshTokenManager의 Redis 구현체 (key: memberId 기준)
 │       └─ oauth
 │           ├─ CustomOAuth2UserService.java       # 구글 등으로부터 사용자 정보 수신 (Spring Security 표준 확장점)
 │           ├─ OAuth2LoginSuccessHandler.java      # 인증 성공 시 회원 연결/가입 + JWT 발급 + 리다이렉트
 │           └─ GoogleOAuthLoginProvider.java        # global.security.OAuthLoginProvider의 구글 구현체
 │
 ├─ global                          # 전역 공통 영역
 │   ├─ config
 │   │   ├─ properties                # JwtProperties.java, CryptoProperties.java, AuthCookieProperties.java (hyunsu.* 커스텀 설정 바인딩)
 │   │   └─ (Security, JPA, Redis, Swagger, QueryDSL 등 설정 클래스)
 │   ├─ security
 │   │   ├─ AuthenticationTokenFilterProvider.java   # 인증 필터 제공 인터페이스 (구체 구현은 auth가 담당)
 │   │   ├─ OAuthLoginProvider.java                   # OAuth2 로그인 구성요소 제공 인터페이스 (구체 구현은 auth가 담당)
 │   │   └─ JwtAuthenticationEntryPoint.java          # 인증 실패 시 ApiResponse 포맷으로 401 응답
 │   ├─ exception
 │   │   ├─ GlobalExceptionHandler.java    # @RestControllerAdvice, 공통 예외 처리
 │   │   ├─ CustomErrorController.java      # 404 등 미처리 에러를 화면/API 경로별로 분기 응답
 │   │   ├─ ErrorCode.java                 # 에러 코드 Enum
 │   │   ├─ CustomException.java           # 커스텀 예외 베이스
 │   │   └─ ErrorResponse.java              # 에러 응답 공통 포맷
 │   └─ common
 │       ├─ response                  # 공통 API 응답 포맷 (ApiResponse.java 등)
 │       └─ util                     # IpAddressResolver.java (요청 IP 추출, 여러 도메인에서 공통 사용)
 │
 └─ SpringbootApplication
```

> 도메인별 커스텀 예외(예: `MemberNotFoundException.java`)는 각 도메인의 `domain` 패키지 안에 두고, `global.exception`의 `CustomException.java`를 상속받는 구조로 통일합니다. 전역에서 잡아 처리하는 부분(`GlobalExceptionHandler.java`)만 `global`에 둡니다.

## 데이터베이스 설계

### ERD 개요

```
member (1) ──< (N) member_history
member (1) ──< (N) member_oauth
```

### member (회원)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGSERIAL | PK |
| username | VARCHAR(20) | 로그인 아이디 (영문/숫자 4~20자, 평문, UNIQUE). 소셜 로그인 전용 가입자는 NULL |
| password | VARCHAR(100) | BCrypt 단방향 암호화 (실제 60자 고정). 소셜 로그인 전용 가입자는 NULL |
| email | VARCHAR(255) | AES-GCM 양방향 암호화된 이메일 (복호화하여 표시/발송에 사용) |
| email_hash | VARCHAR(64) | 이메일 SHA-256 해시 (검색/중복확인용, UNIQUE) |
| role | VARCHAR(20) | 회원 권한 (USER, ADMIN) |
| status | VARCHAR(20) | 계정 상태 (ACTIVE: 정상, LOCKED: 잠김, WITHDRAWN: 탈퇴) |
| login_fail_count | SMALLINT | 로그인 연속 실패 횟수 (5회 도달 시 자동 LOCKED) |
| created_at / updated_at | TIMESTAMP | 등록일 / 수정일 |
| deleted_at | TIMESTAMP | 탈퇴 처리 시각 (soft delete) |

> `email_verified` 컬럼을 별도로 두지 않습니다. 회원가입과 이메일 변경 모두 "이메일 인증 통과 후에만 DB에 저장"되는 흐름으로 설계되어, DB에 저장된 이메일은 항상 인증된 상태이기 때문에 별도 플래그가 불필요합니다.

> `username`, `password`는 V3 마이그레이션에서 nullable로 변경되었습니다. 구글 등 소셜 로그인만으로 가입한 회원은 아이디/비밀번호 자체가 없으며, 로그인은 항상 소셜 로그인 버튼으로만 가능합니다. PostgreSQL은 NULL끼리 서로 다른 값으로 취급하므로, `username`이 NULL인 행이 여러 개 있어도 `UNIQUE` 제약을 위반하지 않습니다.

### member_oauth (소셜 로그인 연동 정보)

회원과 소셜 로그인(Google 등) 계정의 연동 정보를 별도 테이블로 관리합니다. `member` 테이블 자체의 회원가입/로그인 로직을 거의 건드리지 않고 소셜 로그인을 추가할 수 있으며, 한 회원이 여러 provider를 연동하는 것도 자연스럽게 표현할 수 있습니다.

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGSERIAL | PK |
| member_id | BIGINT | FK → member.id |
| provider | VARCHAR(20) | 소셜 로그인 제공자 (GOOGLE 등) |
| provider_id | VARCHAR(255) | 제공자가 발급하는 사용자 고유 식별자 (구글: `sub`) |
| created_at | TIMESTAMP | 연동 시각 |

- `UNIQUE(provider, provider_id)` 제약으로 같은 소셜 계정의 중복 가입을 DB 레벨에서 방지합니다.

### member_history (회원 이력)

로그인 이력과 회원정보 변경 이력을 하나의 테이블에서 `type` 컬럼으로 구분합니다.

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGSERIAL | PK |
| member_id | BIGINT | FK → member.id |
| type | VARCHAR(20) | 이력 유형 (LOGIN, MODIFY) |
| success | BOOLEAN | LOGIN 전용 - 성공 여부 |
| field_name | VARCHAR(50) | MODIFY 전용 - 변경된 필드명 |
| description | VARCHAR(255) | 이력 상세 설명 |
| ip_address | VARCHAR(45) | 요청 IP (IPv6 고려) |
| created_at | TIMESTAMP | 발생 시각 |

### 개인정보 암호화 전략

이메일 등 민감정보를 암호화하면서도 검색/중복확인 기능을 유지하기 위해 다음 방식을 적용합니다.

| 목적 | 방식 | 컬럼 | 구현 |
|---|---|---|---|
| 화면 표시, 메일 발송 | AES-GCM 양방향 암호화 (복호화 가능) | `email` | `AesGcmCryptoService.java` (global.security.crypto) |
| 검색, 중복확인 | SHA-256 결정적 해시 (동일 입력 → 동일 출력, 복호화 불가) | `email_hash` | `EmailHasher.java` (global.security.crypto) |
| 비밀번호 검증 | BCrypt 단방향 해시 | `password` | Spring Security `BCryptPasswordEncoder` |

- 이메일 원문은 AES-GCM으로 암호화하여 저장하고, 화면에 보여주거나 메일을 발송할 때만 복호화합니다.
- 암호화는 매번 무작위 IV를 사용하므로, 같은 이메일이라도 저장될 때마다 다른 암호문이 생성됩니다.
- 회원가입 중복확인, 로그인 시 이메일 기반 조회 등은 `email_hash`(SHA-256)로 처리하여, 원문을 복호화하지 않고도 빠르게 검색할 수 있습니다.
- `email_hash`는 단방향이므로 DB가 유출되더라도 해시만으로는 원본 이메일을 복원할 수 없습니다.
- 비밀번호는 검색이 필요 없는 정보이므로 BCrypt 단방향 암호화만 적용합니다.
- 암호화/해시에 사용하는 키와 salt는 `application-local.yml`의 `crypto.*` 값으로 관리하며, Git에는 포함되지 않습니다.

### 계정 잠금 정책

- 로그인 실패 시 `login_fail_count`를 1씩 증가시킵니다.
- `login_fail_count`가 5에 도달하면 `status`를 `LOCKED`로 변경합니다.
- 로그인 성공 시 `login_fail_count`를 0으로 초기화합니다.

## 인증 / 보안 아키텍처

### Spring Security + JWT

- 모든 API는 `/api`로 시작하는 것을 규칙으로 합니다. (화면 요청과 API 요청을 경로로 구분하기 위함)
- `JwtAuthenticationFilter.java`가 매 요청마다 `Authorization: Bearer {token}` 헤더를 검증해 `SecurityContext`에 인증 정보(memberId, role)를 채웁니다.
- 인증 실패 시 `JwtAuthenticationEntryPoint.java`가 Spring Security 기본 응답 대신, 프로젝트 공통 `ApiResponse.java` 포맷으로 401을 응답합니다.
- 비밀번호는 `BCryptPasswordEncoder`로 단방향 암호화합니다.

### 화면 vs API 에러 응답 분기

- `/api/**` 요청 중 발생한 에러(404 포함) → `ApiResponse.fail()` JSON 응답
- 그 외(화면) 요청 중 발생한 에러 → `404.html` 등 정적 에러 페이지로 forward
- `CustomErrorController.java`가 `/error` 요청을 가로채어, 원래 요청 경로(`/api/**` 여부)에 따라 응답 형식을 분기합니다.

### Controller 응답 타입 규칙

- 헤더 조작이 필요 없는 일반 API는 `ApiResponse<T>`를 그대로 반환합니다. (예: `MemberController`)
- 쿠키 설정, 커스텀 헤더 추가 등 응답 헤더를 직접 다뤄야 하는 API는 `ResponseEntity<ApiResponse<T>>`로 감싸서 반환합니다. (예: `AuthController.login()` - Refresh Token을 `Set-Cookie` 헤더로 내려줘야 함)
- Refresh Token 쿠키의 `Secure` 속성은 `hyunsu.auth.cookie.secure` 프로퍼티로 프로필별 분기합니다. 로컬/개발(HTTP) 환경은 `false`, 운영(HTTPS) 환경은 `true`로 설정합니다. (HTTPS가 아닌 환경에서 `Secure` 쿠키는 브라우저가 저장하지 않으므로 로컬 테스트가 깨지는 것을 방지)

### 커스텀 설정 네임스페이스

프로젝트에서 직접 정의한 설정값(JWT 시크릿, 암호화 키, 쿠키 정책 등)은 `spring.*`, `server.*` 같은 Spring 표준 네임스페이스와 섞이지 않도록, 모두 `hyunsu.*` 네임스페이스 아래에 둡니다.

```yaml
hyunsu:
  jwt:
    secret: ...
    access-token-validity-minutes: 30
    refresh-token-validity-days: 14
  crypto:
    aes:
      secret: ...
      salt: ...
    hash:
      salt: ...
  auth:
    cookie:
      secure: false
```

각 설정 그룹은 `global.config.properties` 패키지의 `@ConfigurationProperties` 클래스(`JwtProperties.java`, `CryptoProperties.java`, `AuthCookieProperties.java`)로 타입 안전하게 바인딩되며, 필요한 곳에서 생성자 주입으로 사용합니다. (개별 `@Value` 주입 대신 이 방식을 우선합니다.)

## 주요 기능

### 회원 / 인증
- 회원가입 (이메일 인증 포함)
- 로그인 (JWT 발급 / 재발급)
- OAuth2 소셜 로그인
- Redis 기반 ID 채번
- Redis 기반 이메일 인증코드 발급 및 만료 처리 (TTL)
- 로그인 실패 5회 시 계정 잠금

### 게시판
- 게시글 CRUD
- 댓글 CRUD (게시글 Aggregate에 종속)
- QueryDSL 기반 검색 / 동적 조건 처리
- 페이징

## 인증 / 토큰 전략

| 토큰 | 저장 위치 | 만료 | 비고 |
|------|-----------|------|------|
| Access Token | 클라이언트 메모리 (응답 바디 전달) | 짧게 (15~30분) | `Authorization: Bearer` 헤더로 전송 |
| Refresh Token | Redis (서버) + HttpOnly Cookie (클라이언트) | 길게 (1~2주) | XSS 방지, 서버에서 강제 만료/폐기 가능 |

- Redis에 Refresh Token을 저장해 로그아웃/강제 만료 시 즉시 무효화 처리
- Refresh Token Redis key는 `memberId` 기준(`refresh-token:{memberId}`)으로, 한 회원당 유효한 토큰이 항상 하나만 존재합니다. 재로그인 시 기존 토큰을 덮어써 **중복 로그인을 방지**합니다.
- Refresh Token은 HttpOnly + Secure + SameSite=Strict 쿠키로 전달해 XSS 방어
- "로그인 상태 유지(remember me)" 옵션은 도입하지 않았습니다. Refresh Token이 이미 자동 로그인 연장 역할을 하므로, 별도 옵션을 두면 보안 정책만 복잡해진다고 판단했습니다.
- 1차로 기본 Access/Refresh 분리 구조를 구현하고, 이후 Refresh Token Rotation(재사용 탐지 포함) 적용 예정

### 토큰 재발급 흐름

Access Token이 만료(401)되었을 때, 화면(JS)에서 페이지 이동마다 무조건 재발급을 시도하지 않고, **실제로 401을 받은 시점에만** 재발급을 시도합니다. (Reactive 방식)

```
1. fetchWithAuth()로 보호된 API 호출 (Authorization 헤더에 Access Token 자동 포함)
2. 서버가 401 응답 (Access Token 만료/무효)
3. 클라이언트가 자동으로 POST /api/auth/reissue 호출 (Refresh Token은 쿠키로 자동 전송)
4. 서버: Refresh Token 유효성(JWT 검증 + Redis 저장값 일치) 확인 후 새 Access Token 발급
5. 클라이언트가 새 Access Token으로 원래 요청을 재시도
6. 재발급마저 실패하면(Refresh Token도 만료/무효) 로그인 페이지로 강제 이동
```

- `static/js/common.js`의 `fetchWithAuth()`가 이 흐름을 캡슐화합니다. 인증이 필요한 화면 API 호출은 `fetch` 대신 이 함수를 사용합니다.
- 재발급 시 Refresh Token은 재사용하며 쿠키를 다시 갱신하지 않습니다 (Rotation 미적용 - 향후 적용 예정).

### OAuth2 소셜 로그인 흐름 (Google)

Spring Security의 OAuth2 Login 기능을 사용합니다. 기본적으로 세션 기반 로그인을 전제로 하지만, 이 프로젝트는 JWT를 사용하므로(`STATELESS`) 인증 성공 직후 흐름을 가로채 우리 JWT를 발급합니다.

```
1. 사용자가 "구글로 로그인" 버튼 클릭 → /oauth2/authorization/google로 이동
2. 구글 로그인 페이지에서 인증 → 구글이 /login/oauth2/code/google로 콜백
3. CustomOAuth2UserService가 구글로부터 사용자 정보(email, sub) 수신
4. OAuth2LoginSuccessHandler:
   a. OAuthLoginCommandService.login()으로 회원 조회/연동/신규가입 처리
      - (provider, providerId)로 기존 연동(member_oauth)이 있으면 그 회원으로 처리
      - 연동이 없지만 같은 이메일의 기존(로컬) 회원이 있으면, 그 회원에 연동만 추가 (계정 통합)
      - 둘 다 없으면 Member.registerByOAuth()로 신규가입 + 연동 생성
   b. Access/Refresh Token 발급, Refresh Token은 Redis 저장 + Cookie 설정 (로그인 API와 동일한 방식)
   c. Access Token을 쿼리 파라미터로 실어 /oauth/success로 리다이렉트
5. /oauth/success 페이지(JS)가 쿼리 파라미터에서 Access Token을 꺼내 메모리에 저장하고,
   보안을 위해 history.replaceState()로 URL에서 토큰 흔적을 제거
```

- 소셜 로그인 전용으로 가입한 회원은 `member.username`, `member.password`가 모두 `NULL`이며, 로그인은 항상 소셜 로그인 버튼으로만 가능합니다 (로컬 로그인 폼으로는 애초에 접근 불가).
- `OAuthLoginCommandService`는 로컬 로그인(`LoginCommandService`)의 비밀번호 검증, 로그인 실패 카운트 등의 정책을 적용하지 않습니다. OAuth 인증 자체가 이미 신뢰할 수 있는 외부 인증이기 때문입니다.
- 화면단: `static/index.html`의 Google 버튼 클릭 시 `login.js`가 `/oauth2/authorization/google`로 이동시킵니다. `/oauth/success`는 `ViewController.java`가 `static/oauth-success.html`로 forward하며, 해당 페이지의 `oauth-success.js`가 토큰 처리를 담당합니다.

## 실행 방법

### 1. 인프라 실행

#### 방법 A. 로컬 수동 구동 (현재 개발 환경)

PostgreSQL과 Redis를 Homebrew로 설치 후, 서비스 자동 등록 없이 수동으로 기동/종료합니다.

**PostgreSQL**
```bash
# 기동
pg_ctl -D /opt/homebrew/var/postgresql@18 start

# 종료
pg_ctl -D /opt/homebrew/var/postgresql@18 stop

# 접속 확인
psql postgres
```

**Redis**
```bash
# 기동
redis-server

# 또는 백그라운드로
redis-server --daemonize yes

# 접속/동작 확인 (PONG 응답이 오면 정상)
redis-cli ping
```

#### 방법 B. Docker Compose

PostgreSQL, Redis를 한번에 실행합니다.

```bash
docker compose up -d
```

### 2. 환경 설정

`application-local.yml.example`을 참고하여 `application-local.yml`을 생성하고, DB / Redis / JWT / 이메일 발송 관련 정보를 입력합니다.

#### JWT 시크릿 생성

`application-local.yml`의 `jwt.secret` 값은 직접 생성한 무작위 문자열을 사용해야 합니다. (예시 값을 그대로 사용하지 않습니다.)

```bash
# macOS 기본 내장 OpenSSL 사용
openssl rand -base64 48
```

생성된 값을 `application-local.yml`에 아래와 같이 입력합니다.

```yaml
hyunsu:
  jwt:
    secret: { 위에서 생성한 값 }
    access-token-validity-minutes: 30
    refresh-token-validity-days: 14
```

#### 개인정보 암호화 키 생성

`application-local.yml`의 `hyunsu.crypto.*` 값(이메일 AES 암호화, SHA-256 해시에 사용)도 직접 생성한 무작위 문자열을 사용해야 합니다.

```bash
# 아래 명령어를 3번 실행해 서로 다른 값 3개를 생성합니다.
openssl rand -base64 32
```

생성된 값 3개를 각각 `application-local.yml`에 입력합니다.

```yaml
hyunsu:
  crypto:
    aes:
      secret: { 1번째로 생성한 값 }
      salt: { 2번째로 생성한 값 }
    hash:
      salt: { 3번째로 생성한 값 }
```

> 주의: `hyunsu.crypto.*` 값은 한 번 정해서 회원 데이터를 저장한 이후에는 변경하면 안 됩니다. 값을 바꾸면 기존에 암호화/해시 처리된 이메일을 복호화하거나 검색할 수 없게 됩니다.

#### Google OAuth2 클라이언트 설정

[Google Cloud Console](https://console.cloud.google.com/)에서 OAuth 클라이언트 ID를 발급받아야 합니다.

1. 프로젝트 생성 (또는 기존 프로젝트 사용)
2. **API 및 서비스 → OAuth 동의 화면**
  - User Type: 외부(External)
  - 테스트 사용자에 본인 Google 계정 추가 (테스트 상태에서는 등록된 테스트 사용자만 로그인 가능)
3. **API 및 서비스 → 사용자 인증 정보 → OAuth 클라이언트 ID 만들기**
  - 애플리케이션 유형: 웹 애플리케이션
  - 승인된 리디렉션 URI: `http://localhost:8080/login/oauth2/code/google`
4. 발급된 클라이언트 ID / 클라이언트 보안 비밀번호를 `application-local.yml`에 입력합니다. (Spring Security OAuth2 Client 표준 네임스페이스이므로 `hyunsu.*`가 아닌 `spring.security.oauth2.*`를 그대로 사용합니다.)

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: { 발급받은 클라이언트 ID }
            client-secret: { 발급받은 클라이언트 보안 비밀 }
            scope:
              - email
              - profile
```

> Google OAuth 클라이언트 발급 및 테스트 모드 사용은 무료입니다. 결제 계정 등록이 필요하지 않으며, 테스트 사용자로 등록한 계정(최대 100명)만 로그인 가능한 상태로도 포트폴리오 시연에는 충분합니다.

> 로컬 환경에서는 실제 메일 발송 대신 콘솔 로그 출력 또는 MailHog 같은 로컬 SMTP 서버를 사용해 이메일 인증 흐름을 테스트할 수 있습니다.

### 3. 빌드 및 실행

```bash
./gradlew bootRun
```

### 4. API 문서 확인

```
http://localhost:8080/swagger-ui/index.html
```

## 테스트 전략

- JUnit5 기반 단위 테스트 / 통합 테스트
- 각 도메인(`member`, `auth`, `board`) 개발과 병행해서 테스트 코드 작성
- 우선순위: 인증/인가(JWT 발급·검증, 토큰 재발급) → 회원가입(이메일 인증 포함) → 게시판 CRUD/검색
- Repository 계층은 H2 기반 통합 테스트, Service 계층은 Mockito 기반 단위 테스트로 분리

```bash
./gradlew test
```

## CI/CD

GitHub Actions를 통해 push/PR 시 빌드 및 테스트를 자동 실행합니다.

## 개발 현황

- [x] DB 마이그레이션 (Flyway) 적용 - 회원/회원이력 테이블
- [x] 패키지 구조 (Domain-First + CQRS) 적용
- [x] 공통 응답 포맷 (ApiResponse) 구현
- [x] 공통 예외 처리 (GlobalExceptionHandler, CustomException, ErrorCode)
- [x] 화면/API 에러 응답 분기 (CustomErrorController, 404 페이지)
- [x] JPA/Redis/QueryDSL/Swagger 설정
- [x] Spring Security + JWT 필터 골격 구현
- [x] Aggregate 설계 원칙 적용 (Member - Aggregate Root, Role/MemberStatus VO 분리)
- [x] 아이디 중복확인 API
- [x] 이메일 인증코드 발송/검증 API (Redis 기반)
- [x] 이메일 AES-GCM 암호화 + SHA-256 해시 검색 구현
- [x] 회원가입 API
- [x] 로그인 API (JWT 발급, Refresh Token Redis 저장 + HttpOnly Cookie)
- [x] 로그인 실패 5회 시 계정 잠금 처리
- [x] 로그인 이력 기록 (member_history)
- [ ] Board-Comment Aggregate 설계 적용
- [x] 토큰 재발급 API
- [ ] 로그아웃 API
- [x] 소셜 로그인 DB 스키마 설계 (member_oauth 테이블, member.username/password nullable 변경)
- [x] OAuth2 소셜 로그인 (Google) 백엔드 구현
- [x] OAuth2 소셜 로그인 화면(/oauth/success, 구글 로그인 버튼) 연동
- [ ] 게시판 CRUD
- [ ] 댓글 CRUD
- [ ] QueryDSL 검색 / 페이징
- [ ] Swagger API 문서 보강 (각 API에 어노테이션 추가)
- [ ] 테스트 코드 작성
- [ ] Docker Compose 환경 구성
- [ ] GitHub Actions CI/CD 구성

## 참고

- 도서: 신선영, 『스프링 부트 4 개발자 되기 + AI: 자바 편』
- 책의 권장 환경(Java 25, H2)보다 최신 버전(Java 26, PostgreSQL)을 적용하여 실무 환경에 가깝게 구성했습니다.