# ARTINUS 구독 서비스 백엔드 — 제품 요구사항 및 설계 문서

## 1. 개요

구독 상태를 관리하는 백엔드 API를 제공한다. 회원은 복수의 채널을 통해 구독/해지를 수행하며, 상태 변경은 외부 난수 API의 응답을 관문(gate)으로 삼아 확정된다. 회원은 자신의 구독 이력과 LLM이 생성한 자연어 요약을 조회할 수 있다.

## 2. 도메인 정의

### 2.1 구독 상태

회원은 아래 3가지 상태 중 하나를 가진다.

| 상태 | 설명 |
|---|---|
| `NONE` | 구독하지 않은 상태 |
| `GENERAL` | 일반 등급 구독 |
| `PREMIUM` | 프리미엄 등급 구독 |

### 2.2 채널

채널은 구독/해지가 이루어지는 접점이며, 각 채널은 아래 3가지 역량 중 하나를 가진다.

| 역량 | 구독 | 해지 |
|---|---|---|
| `BOTH` | O | O |
| `SUBSCRIBE_ONLY` | O | X |
| `UNSUBSCRIBE_ONLY` | X | O |

본 시스템의 채널 인스턴스는 다음과 같다.

| 채널 | 역량 |
|---|---|
| 홈페이지 | `BOTH` |
| 모바일앱 | `BOTH` |
| 네이버 | `SUBSCRIBE_ONLY` |
| SKT | `SUBSCRIBE_ONLY` |
| 콜센터 | `UNSUBSCRIBE_ONLY` |
| 이메일 | `UNSUBSCRIBE_ONLY` |

### 2.3 상태 전이 규칙

구독 요청(`SUBSCRIBE`):

| 현재 상태 | 변경 가능 상태 |
|---|---|
| `NONE` | `GENERAL`, `PREMIUM` |
| `GENERAL` | `PREMIUM` |
| `PREMIUM` | (불가) |

해지 요청(`UNSUBSCRIBE`):

| 현재 상태 | 변경 가능 상태 |
|---|---|
| `PREMIUM` | `GENERAL`, `NONE` |
| `GENERAL` | `NONE` |
| `NONE` | (불가) |

### 2.4 외부 난수 API (csrng)

구독/해지 요청은 아래 외부 API의 응답을 받아 확정 여부가 결정된다.

```
GET https://csrng.net/csrng/csrng.php?min=0&max=1
→ [{ "status": "success", "min": 0, "max": 1, "random": 0|1 }]
```

- `random == 1`: 도메인 상태 전이 확정(트랜잭션 커밋에 대응)
- `random == 0`: 도메인 상태 전이 거부(트랜잭션 롤백에 대응)

## 3. 기능 요구사항

### 3.1 구독 API

- `POST /api/v1/subscriptions`
- 요청: `phoneNumber`, `channelId`, `targetState`
- 채널이 구독 가능(`BOTH` 또는 `SUBSCRIBE_ONLY`)해야 한다.
- 현재 상태에서 목표 상태로의 구독 전이가 허용되어야 한다.
- 최초 회원은 `NONE`, `GENERAL`, `PREMIUM` 중 어떤 상태로도 가입할 수 있다.
- 외부 난수 API 응답에 따라 전이 여부가 결정된다.

### 3.2 해지 API

- `POST /api/v1/subscriptions/cancel`
- 요청: `phoneNumber`, `channelId`, `targetState`
- 채널이 해지 가능(`BOTH` 또는 `UNSUBSCRIBE_ONLY`)해야 한다.
- 현재 상태에서 목표 상태로의 해지 전이가 허용되어야 한다.
- 외부 난수 API 응답에 따라 전이 여부가 결정된다.

### 3.3 이력 조회 API

- `GET /api/v1/subscriptions/history?phoneNumber={...}`
- 응답: 해당 회원의 구독/해지 이력 목록과 LLM이 생성한 자연어 요약.

응답 구조:

```json
{
  "phoneNumber": "010-1234-5678",
  "history": [
    {
      "occurredAt": "2026-01-01T10:00:00Z",
      "channel": "홈페이지",
      "operation": "SUBSCRIBE",
      "from": "NONE",
      "to": "GENERAL",
      "outcome": "SUCCEEDED"
    }
  ],
  "summary": "2026년 1월 1일 홈페이지를 통해 일반 구독으로 가입한 뒤, ...",
  "summaryStatus": "GENERATED"
}
```

- `outcome`: `SUCCEEDED` 또는 `DENIED_BY_GATE`
- `summaryStatus`: `GENERATED` 또는 `UNAVAILABLE` (LLM 호출 실패 시)

## 4. 제약 및 가정

- 전화번호는 국내 형식(`01X-XXXX-XXXX` 또는 11자리 숫자)만 허용한다.
- 회원 식별자는 전화번호 하나이며, 별도 회원 ID나 인증 체계를 도입하지 않는다.
- API Key 및 시크릿은 레포지토리에 포함되지 않는다.
- 기본 실행 모드는 stub이며, 외부 자격증명 없이 전체 기능이 동작해야 한다.
- 외부 난수 API(csrng)와 LLM API 연동은 port를 통한 인터페이스 계약으로 표현되며, 실제 엔드포인트 연결은 프로파일 전환으로 활성화된다.

## 5. 아키텍처

### 5.1 전체 구조

레이어는 4개이며, Bounded Context를 최상위 패키지로 두고 그 내부에 레이어를 배치한다.

```
com.artinus
├── subscription                   # Bounded Context
│   ├── controller
│   ├── service
│   │   └── port                   # 도메인이 요구하는 인터페이스
│   ├── repository
│   └── domain                     # 순수 Java, 프레임워크 비의존
├── history                        # Bounded Context
│   ├── controller
│   ├── service
│   │   └── port
│   ├── repository
│   └── domain
└── plugin                         # 외부 의존성 격리
    ├── csrng
    ├── llm
    └── stub
```

### 5.2 Bounded Context

- **Subscription**: 회원별 현재 구독 상태의 명령 처리. Aggregate로서 상태 전이 규칙과 불변식을 보유한다.
- **History**: 구독/해지 이력의 조회와 LLM 요약 제공. 읽기 중심 모델이며 `SubscriptionEvent`를 append-only로 다룬다.

두 컨텍스트는 상호 import하지 않는다. 교차 관심사는 각자의 Repository를 통해 동일 DB 스키마를 공유하되, 모델링·API 계약은 분리된다.

### 5.3 Plugin 격리 전략

모든 외부 시스템 의존성은 `plugin` 패키지 하위에 격리한다. 격리는 아래 세 규칙으로 보장된다.

1. 인터페이스는 도메인 측(`<context>/service/port/`)에서 정의하고, plugin이 이를 구현한다.
2. plugin 패키지 내부 타입(HTTP 클라이언트, 외부 응답 DTO 등)은 package-private 접근자로 선언되어 plugin 패키지 밖에서 import할 수 없다. 오직 port 구현체만 public이다.
3. port 인터페이스의 시그니처에는 HTTP/JSON/프레임워크 타입이 포함되지 않는다. 도메인 언어만 사용한다.

### 5.4 의존 방향

```
controller → service → domain
                   ↘
                    port (interface)
                      ↑ implements
                    plugin
```

- 도메인과 application은 plugin의 구체 구현을 알지 않는다.
- Spring DI가 런타임에 port 구현체를 바인딩한다.
- Bounded Context 간 상호 참조는 금지한다.

## 6. 도메인 모델

### 6.1 Aggregate

**`Subscription`** (Aggregate Root)
- Identity: `PhoneNumber`
- State: `SubscriptionState`
- Version: `@Version` 기반 낙관적 락
- Behavior: `changeTo(targetState, channel, operation)` — 상태 전이 규칙과 채널 역량 검증을 내부에서 수행하고 도메인 이벤트를 반환한다.

**`SubscriptionEvent`** (Entity, append-only)
- Identity: `id`
- Fields: `phoneNumber`, `from`, `to`, `channelId`, `operation`, `outcome`, `occurredAt`
- 별도 Aggregate로 분리되며, Subscription 상태 변경과 동일 트랜잭션에서 저장된다.

### 6.2 Value Object

| VO | 유효성 |
|---|---|
| `PhoneNumber` | 한국 휴대폰 형식 검증, 생성자에서 강제 |
| `SubscriptionState` | enum (`NONE`, `GENERAL`, `PREMIUM`), `canTransitionTo(target, operation)` 규칙 메서드 포함 |
| `ChannelCapability` | enum (`BOTH`, `SUBSCRIBE_ONLY`, `UNSUBSCRIBE_ONLY`), `allows(operation)` 메서드 포함 |
| `Operation` | enum (`SUBSCRIBE`, `UNSUBSCRIBE`) |

### 6.3 Channel

채널은 Java enum으로 정의한다. 각 상수는 `id`, `displayName`, `capability`를 가진다. 요청에는 `channelId`로 수신하며 내부에서 enum으로 매핑한다.

```java
public enum Channel {
    HOMEPAGE   (1L, "홈페이지", ChannelCapability.BOTH),
    MOBILE_APP (2L, "모바일앱", ChannelCapability.BOTH),
    NAVER      (3L, "네이버",   ChannelCapability.SUBSCRIBE_ONLY),
    SKT        (4L, "SKT",      ChannelCapability.SUBSCRIBE_ONLY),
    CALL_CENTER(5L, "콜센터",   ChannelCapability.UNSUBSCRIBE_ONLY),
    EMAIL      (6L, "이메일",   ChannelCapability.UNSUBSCRIBE_ONLY);
    // ...
}
```

채널별 고유 정책이 추가되면 enum 상수 오버라이드 또는 Strategy 승격으로 확장한다. 채널 변경은 본질적으로 코드 변경을 동반하므로 데이터로 분리하지 않는다.

### 6.4 상태 전이 규칙

상태 전이 규칙은 `SubscriptionState.canTransitionTo(target, operation)` 내부에 캡슐화한다. 전이 진입점은 `Subscription.changeTo(...)` 하나이며, 이곳에서 규칙 위반·채널 역량 불일치를 예외로 거절한다.

```java
public SubscriptionChanged changeTo(
        SubscriptionState target, Channel channel, Operation op) {
    if (!channel.supports(op))
        throw new ChannelNotAllowedException(channel, op);
    if (!this.state.canTransitionTo(target, op))
        throw new IllegalStateTransitionException(this.state, target, op);

    SubscriptionState previous = this.state;
    this.state = target;
    return new SubscriptionChanged(
        this.phoneNumber, previous, target, channel.id(), op, Instant.now());
}
```

## 7. 외부 시스템 통합 및 장애 대응

### 7.1 Gate 모델

외부 난수 API는 도메인 트랜잭션의 관문(gate)으로 사용되며, DB 트랜잭션 외부에서 호출된다. 커넥션·락은 네트워크 I/O 구간에 점유되지 않는다.

```
[1] 도메인 검증 (트랜잭션 외)
    - PhoneNumber 형식
    - Channel 역량
    - 상태 전이 가능 여부

[2] Gate 호출 (트랜잭션 외, Resilience4j wrap)
    RandomGate.request() → ALLOWED | DENIED | (예외)

[3] 결과 반영
    ALLOWED    → @Transactional { 상태 변경 + 이벤트 저장(SUCCEEDED) }
    DENIED     → @Transactional { 이벤트 저장(DENIED_BY_GATE) }
    예외       → 응답 5xx, 상태·이력 변경 없음
```

### 7.2 장애 대응 패턴

Resilience4j의 3가지 패턴을 gate 호출 지점에 적용한다.

| 패턴 | 설정값 |
|---|---|
| Timeout | 2초 |
| Retry | 최대 3회, exponential backoff + jitter (100ms → 200ms → 400ms) |
| Circuit Breaker | sliding window 10, 실패율 50% 초과 시 OPEN, 30초 후 HALF_OPEN |

설정은 `application.yml`의 `resilience4j` 섹션으로 외부화한다. 어노테이션(`@TimeLimiter`, `@Retry`, `@CircuitBreaker`)을 plugin의 port 구현체에 부착한다.

### 7.3 에러 응답 분류

| 상황 | HTTP | body |
|---|---|---|
| 도메인 검증 실패 | 400 | `ProblemDetail` (RFC 7807) |
| Gate DENIED (`random == 0`) | 200 | `outcome: DENIED_BY_GATE` |
| Gate 호출 실패 (timeout, circuit open, 네트워크) | 503 | `ProblemDetail`, `Retry-After` 헤더 |

Gate DENIED는 외부 시스템이 정상 응답한 "비즈니스 거부"이므로 2xx로 분류한다. Gate 호출 자체의 실패만 5xx로 분류한다.

### 7.4 LLM 호출 실패 처리

이력 조회에서 LLM 호출이 실패하면 history 목록은 정상 반환하고 `summary`는 `null`, `summaryStatus`는 `UNAVAILABLE`로 응답한다. 핵심 기능(이력 조회)의 가용성은 부가 기능(요약)의 장애에 영향받지 않는다.

## 8. 이력 조회 및 LLM 요약

### 8.1 요약 생성 시점

요약은 조회 시점에 동기적으로 생성한다. 캐싱 및 사전 생성은 도입하지 않는다. 확장이 필요할 경우 `@Cacheable`/`@CacheEvict` 또는 비동기 pre-compute로 전환 가능하다.

### 8.2 요약 대상 범위

- 대상 이벤트: 최근 20건.
- 필터: `outcome == SUCCEEDED`만 요약 대상에 포함한다. `DENIED_BY_GATE` 이벤트는 history 목록에는 노출되나 요약에는 반영되지 않는다.
- 상한은 `application.yml`의 `history.summary.event-limit` 프로퍼티로 조정 가능하다.

### 8.3 Pagination

이력 목록은 offset/limit 기반 pagination을 제공한다. 대용량 이력 대응 시 cursor-based로 전환할 수 있다.

## 9. 비기능 요구사항 (가정)

| 항목 | 목표값 |
|---|---|
| 가용성 | 99.9% |
| 피크 처리량 | 100 TPS |
| Latency p99 | 외부 API 제외 500ms 미만, 전체 2s 미만 |
| 이력 보존 | `SubscriptionEvent` 3년 |
| 복구 목표 | RTO 30분, RPO 15분 |

## 10. 기술 스택

| 구분 | 선택 |
|---|---|
| 언어 | Java 21 (LTS) |
| 프레임워크 | Spring Boot 3.5.3 |
| 데이터베이스 | MySQL 8 |
| ORM | Spring Data JPA |
| 마이그레이션 | Flyway |
| API 문서 | springdoc-openapi (Swagger UI) |
| 장애 대응 | Resilience4j |
| 테스트 | JUnit 5, AssertJ, Testcontainers (MySQL) |
| 커버리지 | JaCoCo |
| 빌드 | Gradle |
| 컨테이너 | Docker (multi-stage) |
| CI/CD | GitHub Actions |
| 레지스트리 | GHCR |
| 배포 | GKE + ArgoCD (GitOps, 가정) |

## 11. 테스트 전략

### 11.1 원칙

- Classicist(Detroit) 학파를 채택한다. Mockito mock은 port 인터페이스에 한해서만 사용하고, 도메인·application 내부 협력객체는 실제 인스턴스를 사용한다.
- "Managed dependency"(직접 제어 가능한 의존)는 실제 인스턴스로, "Unmanaged dependency"(외부 시스템)는 stub 또는 mock으로 격리한다.
- 테스트 이름은 검증하는 행위를 서술한다 (`should_{동작}_{조건}` 형식).

### 11.2 테스트 피라미드

| 레벨 | 대상 | 도구 |
|---|---|---|
| 단위 | `SubscriptionState`, `Subscription`, `Channel`, `PhoneNumber`, `SubscriptionEvent` | JUnit 5, AssertJ |
| 통합 | `SubscriptionService`, `HistoryService` | `@SpringBootTest`, Testcontainers(MySQL), stub plugin |
| 컨트롤러 | HTTP 계약, 요청 검증, 응답 포맷 | `@WebMvcTest`, MockMvc, `@MockBean` |
| 회복성 | Resilience4j 적용 결과 | stub에 지연·예외 주입 |

### 11.3 Testcontainer

MySQL은 Testcontainers로 실제 인스턴스를 기동한다. 인메모리 대체(H2)는 방언 차이로 인한 가짜 성공을 유발하므로 사용하지 않는다.

### 11.4 Stub

외부 의존성(csrng, LLM)은 stub 구현체를 port의 1급 구현체로 취급한다. 기본 실행 프로파일에서 활성화되며, 테스트도 동일 stub을 사용한다.

- `StubRandomGate`: 확률적 `ALLOWED`/`DENIED`, 레이턴시 시뮬레이션 포함.
- `StubHistorySummarizer`: 이벤트 시퀀스를 한국어 템플릿으로 자연어 조합.
- 결정론적 제어가 필요한 테스트에서는 `DeterministicRandomGate(GateResult fixed)`와 같은 테스트 전용 구현체를 주입한다.

### 11.5 커버리지

JaCoCo 전체 70% 라인 커버리지를 하한으로 강제한다. 미달 시 `./gradlew check`가 실패한다. 설정, 단순 DTO, 애플리케이션 진입점은 계측에서 제외한다.

### 11.6 격리 규칙 보장

아키텍처 격리는 JVM 접근 제어(plugin 내부 타입의 package-private 선언)와 코드 리뷰로 유지한다.

## 12. 배포 및 인프라

### 12.1 컨테이너화

Multi-stage Dockerfile로 JDK 21 빌드 후 JRE 21 alpine 이미지로 전환한다. 컨테이너는 non-root 사용자로 실행한다.

### 12.2 CI/CD

GitHub Actions 워크플로우는 다음 단계로 구성한다.

1. 테스트 실행 및 JaCoCo 커버리지 검증.
2. Docker 이미지 빌드 (buildx 캐시 활용).
3. GHCR에 `latest` 및 커밋 SHA 태그로 푸시.
4. 별도 GitOps 레포지토리의 Kustomize manifest 이미지 태그를 업데이트.

레지스트리 인증은 `GITHUB_TOKEN`을 사용한다. 외부 인프라 자격증명은 OIDC 기반 Workload Identity Federation으로 주입하며, static 키를 저장하지 않는다.

### 12.3 배포 플랫폼

GKE 클러스터에 ArgoCD(GitOps)로 배포한다. GitOps 레포지토리의 매니페스트가 단일 진실공급원(single source of truth)이며, ArgoCD는 이를 주기/이벤트 기반으로 클러스터에 동기화한다.

- 배포 전략: Kubernetes Rolling Update.
- 롤백: GitOps 레포지토리의 git revert.
- Pod 스펙: requests `500m`/`512Mi`, limits `1000m`/`1Gi`.
- Replica: 2 이상, HPA 기반 자동 확장.

본 저장소가 제공하는 산출물은 `Dockerfile`과 `.github/workflows/*.yml`로 한정되며, K8s 매니페스트·ArgoCD Application·클러스터 IaC는 본 과제 범위 외로 가정한다.

### 12.4 데이터 계층

Cloud SQL for MySQL을 regional HA 구성으로 사용한다. 애플리케이션은 Cloud SQL Auth Proxy(sidecar) 또는 private IP 경로로 접속한다. 백업은 관리형 자동 백업 및 Point-in-Time Recovery에 의존한다.

### 12.5 시크릿

- LLM API key 등 시크릿은 GCP Secret Manager에 저장하고, Workload Identity를 통해 Pod 환경변수로 주입한다.
- 레포지토리에는 어떤 시크릿도 포함하지 않는다. `application-real-*.yml` 파일명 패턴은 `.gitignore`로 차단한다.

### 12.6 관측

- 애플리케이션 로그는 구조적 JSON으로 출력하고 Cloud Logging으로 수집한다.
- Micrometer로 계측한 메트릭은 Cloud Monitoring으로 송출한다.
- Resilience4j의 회로 차단기 상태, gate 호출 성공률, LLM 호출 성공률을 주요 메트릭으로 노출한다.
- 전화번호 등 PII는 로그에서 마스킹(`010-****-5678`) 처리한다.

## 13. 평가 항목 매핑

| 평가 항목 | 본 문서의 대응 섹션 |
|---|---|
| 아키텍처 설계 및 프로젝트 구성 | 5, 6 |
| 요구사항 이해 | 2, 3 |
| API 설계 및 구현 | 3, 7.3 |
| 외부 API 장애 대응 | 7 |
| 클라우드 인프라 설계 | 12 |

## 14. 범위 외

- 이벤트 소싱 및 CQRS 도입.
- 멱등성 키 기반 중복 요청 방지(표준 패턴 인지, 구현 제외).
- 이력 요약 사전 생성 및 캐싱.
- Bulkhead 패턴, Argo Rollouts 기반 카나리/블루그린.
- WAF, CDN, Memorystore(Redis).
- 다중 리전 재해 복구.
- K8s 매니페스트 및 클러스터 프로비저닝.

## 15. 확장 포인트

본 설계가 의도적으로 구현 범위에서 제외한 항목은 아래 지점으로 확장 가능하다.

- **캐싱**: `HistoryService.query()`에 `@Cacheable(key = "phoneNumber + lastEventId")` 적용, 이벤트 append 시 `@CacheEvict`.
- **멱등성**: `Idempotency-Key` 헤더 수신, `(key, request_hash) → response` 매핑을 TTL 캐시로 저장.
- **채널 정책 다형화**: `Channel` enum 상수에 메서드 override 또는 `ChannelPolicy` 인터페이스로 승격.
- **이력 요약 pre-compute**: `SubscriptionChanged` 이벤트 구독 비동기 워커를 도입하고, 요약 저장 테이블을 분리.
- **카나리 배포**: Argo Rollouts 도입.
- **외부 RNG 이중화**: `RandomGate`에 대한 fallback plugin 추가 및 Strategy 전환.
