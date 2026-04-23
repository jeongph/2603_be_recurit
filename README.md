# ARTINUS 구독 서비스

구독 상태를 관리하는 백엔드 API. 구독/해지/이력 조회와 LLM 기반 이력 요약을 제공한다.

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
- Docker (multi-stage)

## 실행

### 기본 실행 (Stub 모드)

외부 API 자격증명 없이 전체 기능이 동작한다.

```bash
# MySQL 기동 (Testcontainers 가 테스트에서 자동 기동하므로 테스트에는 불필요)
docker run --rm -d --name artinus-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=artinus \
  -e MYSQL_USER=artinus \
  -e MYSQL_PASSWORD=artinus \
  -p 3306:3306 mysql:8.0

./gradlew bootRun
```

### 컨테이너 실행 (선택)

```bash
docker build -t artinus-subscription:local .
docker run --rm -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_USERNAME=artinus \
  -e DB_PASSWORD=artinus \
  artinus-subscription:local
```

MySQL 컨테이너와 동일 네트워크로 묶거나 `host.docker.internal` 로 호스트의 MySQL 을 가리킬 수 있다.

### 테스트

```bash
./gradlew test              # 전체 단위 + 통합 테스트
./gradlew check             # test + JaCoCo 라인 커버리지 70% 검증
./gradlew smokeTest         # 실제 csrng.net 호출 검증 (선택)
```

Testcontainers 가 MySQL 8 인스턴스를 자동 기동한다.

### 실제 외부 연동 활성화

```bash
export ANTHROPIC_API_KEY=sk-ant-...
SPRING_PROFILES_ACTIVE=real-csrng,real-llm ./gradlew bootRun
```

프로파일을 선택적으로 조합할 수 있다.

- `real-csrng` 만 활성화: 난수 API 는 실제 호출, LLM 요약은 stub 유지
- `real-llm` 만 활성화: LLM 은 Claude API, 난수 API 는 stub 유지

## API 문서

기동 후 Swagger UI: `http://localhost:8080/swagger-ui.html`

OpenAPI 정의: `http://localhost:8080/v3/api-docs`

## 주요 엔드포인트

| 메서드 | 경로 | 설명 |
|---|---|---|
| POST | `/api/v1/subscriptions` | 구독 신청 |
| POST | `/api/v1/subscriptions/cancel` | 구독 해지 |
| GET | `/api/v1/subscriptions/history` | 이력 조회 + LLM 요약 |

요청/응답 스키마는 Swagger UI 에서 확인한다.

## 아키텍처

### Bounded Context
- `subscription` — 상태 변경 중심(명령). Aggregate 로서 상태 전이 규칙 및 불변식을 보유.
- `history` — 이력 조회 및 LLM 요약(읽기). `SubscriptionEvent` 를 append-only 로 다룬다.

### 레이어링
- `controller → service → domain` 방향.
- `plugin/` 패키지가 외부 시스템 의존성(csrng, LLM)을 격리. 인터페이스는 도메인 측(`service/port/`)이 정의.
- 두 Bounded Context 는 상호 import 하지 않는다. 단 `subscription.domain` 의 값 객체/enum/도메인 이벤트는 Shared Kernel 로 취급하여 `history` 가 참조한다.

### Gate 모델
외부 난수 API(csrng) 호출은 DB 트랜잭션 외부에서 수행된다. 네트워크 I/O 구간에 DB 커넥션/락을 점유하지 않는다. 응답 결과에 따라 성공 경로 또는 거부 이력 기록 경로의 트랜잭션을 단일 단위로 연다.

### 에러 응답 분류
| 상황 | HTTP | 응답 |
|---|---|---|
| 도메인 검증 실패 (전화번호·채널·상태전이) | 400 | `ProblemDetail` |
| Gate 가 random=0 으로 거부 | 200 | `outcome: DENIED_BY_GATE` |
| Gate 호출 실패 (timeout·회로차단·네트워크) | 503 | `ProblemDetail` + `Retry-After` 헤더 |
| LLM 요약 실패 | 200 | `summary: null`, `summaryStatus: UNAVAILABLE` |

설계 배경은 `docs/prd.md` 참조.

## 제약 / 가정

- **전화번호**: 국내 휴대폰 형식(`010-XXXX-XXXX` 11자리 또는 `011/016-019-XXX-XXXX` 10자리)만 허용. 국제번호·유선번호는 범위 외.
- **채널**: `HOMEPAGE`, `MOBILE_APP`, `NAVER`, `SKT`, `CALL_CENTER`, `EMAIL` 6종을 enum 으로 고정. 채널별 정책 차이를 수용하기 위해 데이터가 아닌 코드로 관리.
- **기본 실행**: stub 모드. csrng stub 은 `Random` 기반으로 ALLOWED/DENIED 를 확률적으로 반환하며 50-200ms 레이턴시를 시뮬레이션. LLM stub 은 이벤트 시퀀스를 한국어 템플릿으로 조합.
- **이력 요약 대상**: 성공(`SUCCEEDED`) 이벤트 최근 20건. `history.summary.event-limit` 프로퍼티로 조정.
- **트랜잭션 커밋/롤백 해석**: 요구사항의 "트랜잭션" 을 단일 DB 트랜잭션이 아닌 비즈니스 트랜잭션 의미로 해석. `random=1` 은 상태 전이 확정, `random=0` 은 상태 전이 거부로 대응.
- **K8s/ArgoCD**: 배포 매니페스트 및 ArgoCD Application 은 별도 GitOps 리포지토리에서 관리되는 것을 가정. 본 저장소는 Dockerfile 및 GitHub Actions 워크플로까지 제공한다.

## 배포

- 컨테이너 이미지: GHCR (`ghcr.io/<owner>/<repo>`), 태그는 커밋 SHA 고정.
- CI/CD: `.github/workflows/deploy.yml`
  - `test` — 모든 push/PR 에서 `./gradlew check` 실행 + JaCoCo 리포트 아티팩트 업로드.
  - `build-and-push` — `main` push 시 Docker 빌드 및 GHCR 푸시 (buildx + GHA 캐시).
  - `update-gitops` — GitOps 레포지토리의 kustomization.yaml 이미지 태그 갱신(placeholder).
- 런타임: ArgoCD 가 GitOps 레포를 감지하여 GKE 클러스터에 동기화하는 것을 가정.

## 디렉토리 구조

```
src/main/java/com/artinus/
├── subscription/                 # Bounded Context (쓰기)
│   ├── controller/
│   ├── service/
│   │   └── port/                 # 도메인이 요구하는 인터페이스
│   ├── repository/
│   └── domain/                   # 순수 Java + JPA 어노테이션
├── history/                      # Bounded Context (읽기 + LLM 요약)
│   ├── controller/
│   ├── service/
│   │   └── port/
│   ├── repository/
│   └── domain/
├── plugin/                       # 외부 시스템 격리
│   ├── csrng/                    # 실 구현 @Profile("real-csrng")
│   ├── llm/                      # 실 구현 @Profile("real-llm")
│   └── stub/                     # 기본 활성 @Profile("stub")
├── common/                       # 전역 예외 핸들러, 설정, 로깅 유틸
└── Application.java
```

## 확장 포인트 (본 구현에서 제외)

확장 시나리오 상세는 `docs/prd.md` §15 참조.
