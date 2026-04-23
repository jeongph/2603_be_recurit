# CLAUDE.md

본 파일은 Claude Code 세션에서 자동 로드되는 프로젝트 개발 지침이다.

## 프로젝트

ARTINUS 구독 서비스 백엔드. 상세 설계는 `docs/prd.md` 참고.

## 기술 스택

- Java 21 (LTS)
- Spring Boot 3.5.3
- MySQL 8
- Spring Data JPA, Flyway
- Resilience4j
- springdoc-openapi (Swagger UI)
- JUnit 5, AssertJ, Testcontainers
- JaCoCo
- Gradle

## 패키지 구조

Bounded Context를 최상위, 그 아래 layer로 배치한다.

```
com.artinus
├── subscription
│   ├── controller
│   ├── service
│   │   └── port
│   ├── repository
│   └── domain
├── history
│   ├── controller
│   ├── service
│   │   └── port
│   ├── repository
│   └── domain
└── plugin
    ├── csrng
    ├── llm
    └── stub
```

## 아키텍처 규칙

### 의존 방향
- `controller` → `service` → `domain`
- `service` → `port`(interface). `service`는 plugin의 구체 구현을 알지 않는다.
- `plugin` → `port`(implements)
- Bounded Context(`subscription`, `history`)는 상호 import하지 않는다.

### 도메인 레이어
- `domain` 패키지는 Java 표준 라이브러리와 JPA 어노테이션 외 의존이 없다. Spring annotation, Spring Data, HTTP, JSON 라이브러리 import를 허용하지 않는다.
- 도메인 모델은 Rich Domain Model로 작성한다. 상태 전이와 불변식은 Aggregate 메서드 내부에서 강제한다. Application Service는 조립만 담당한다.
- Aggregate 메서드는 도메인 이벤트를 반환한다.

### Plugin 격리 규칙
- `plugin/<external>/` 패키지 내부 타입(HTTP 클라이언트, 외부 응답 DTO 등)은 package-private 접근자로 선언한다.
- port 구현체(예: `CsrngRandomGate`)만 public으로 노출한다.
- port 인터페이스 시그니처는 도메인 언어만 사용한다. `HttpStatus`, `JsonNode`, `RestTemplate` 등 외부 타입은 노출하지 않는다.
- plugin 패키지는 `subscription`, `history` 내부에서 import할 수 없다.

### 예외 처리
- 도메인 위반은 도메인 고유 예외로 표현한다 (`IllegalStateTransitionException`, `ChannelNotAllowedException` 등 `RuntimeException` 하위).
- HTTP 응답 매핑은 `@ControllerAdvice` + `ProblemDetail` (RFC 7807)로 일원화한다.

## 외부 의존성과 Stub 전략

- 기본 실행 프로파일은 stub이다. 외부 자격증명 없이 `./gradlew bootRun` 으로 전체 기능이 동작해야 한다.
- 실제 외부 연동은 Spring 프로파일로 활성화한다: `real-csrng`, `real-llm`.
- `application-real-*.yml`, `application-local*.yml`은 `.gitignore`로 차단된다.
- stub 구현체는 테스트 보조물이 아니라 port의 1급 구현체이며, 실제와 유사한 응답(확률적/템플릿 기반)을 제공한다.

## 테스트

- Classicist(Detroit) 학파. Mockito mock은 port 인터페이스에 한정한다.
- 도메인 테스트: Spring 없이 순수 JUnit 5 + AssertJ.
- 통합 테스트: `@SpringBootTest` + Testcontainers(MySQL 8). 인메모리 DB(H2) 대체를 사용하지 않는다.
- 컨트롤러 테스트: `@WebMvcTest` + `@MockBean`. HTTP 계약만 검증한다.
- JaCoCo 전체 라인 커버리지 70% 하한. `./gradlew check` 에서 미달 시 빌드 실패.
- 테스트 이름은 `should_{행위}_{조건}` 형식으로 행위를 서술한다.

## 실행

| 명령 | 용도 |
|---|---|
| `./gradlew bootRun` | stub 프로파일로 기동 |
| `./gradlew test` | 전체 테스트 |
| `./gradlew check` | 테스트 + 커버리지 검증 |
| `./gradlew bootJar` | 실행 가능한 JAR 빌드 |

## 개발 워크플로

1. 기능 단위로 feature 브랜치를 생성한다. 브랜치 네이밍은 `feature/<짧은-설명>`.
2. 커밋은 논리적 단위로 분할한다. 하나의 커밋에 여러 관심사를 섞지 않는다.
3. 기능 구현을 완료하면 `kent-beck` 에이전트에 코드 리뷰를 요청한다.
4. 리뷰에서 지적된 치명적/개선 필요 항목을 반영한 후 기능을 완료 처리한다.
5. PR을 생성하지 않고 로컬에서 `main`에 merge 한 뒤 push 한다.

## 커밋 컨벤션

- `feat:` 새 기능
- `fix:` 버그 수정
- `refactor:` 동작 변경 없는 구조 개선
- `test:` 테스트 전용 변경
- `docs:` 문서 변경
- `chore:` 빌드/의존성/설정 등

커밋 메시지는 한국어로 작성한다. 제목은 72자 이내. 본문은 필요 시 빈 줄 뒤에 추가한다.

## 문서

- `docs/prd.md`: 공식 제품 요구사항 및 설계 문서. 레포지토리에 포함된다.
- `docs/` 하위 그 외 파일(브레인스토밍, 내부 메모): `.gitignore`로 차단된다. 레포지토리에 포함되지 않는다.
- `README.md`: 구현 완료 시점에 별도 작성한다. 프로젝트 소개, 실행 방법, API 문서 링크, 제약사항을 포함한다.
