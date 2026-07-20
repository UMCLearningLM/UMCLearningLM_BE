# 📗 LearningLM Backend

AI를 단순 질문/답변 도구에서 실제 작업 흐름을 설계하는 도구로 확장하는 학습 플랫폼

LearningLM의 백엔드 저장소입니다.
React/TypeScript 기반 프론트엔드와 통신하며, 튜토리얼·Flow 워크플로우·저장/공유 기능을 위한 REST API를 제공합니다.
Java, Spring Boot를 기반으로 하며, 유지보수성을 위해 도메인 단위(Domain-based) 아키텍처를 따릅니다.

---

## 🛠 Tech Stack

| 분류 | 기술 | 비고 |
| --- | --- | --- |
| Core | Java 17, Spring Boot 3.x | 백엔드 핵심 프레임워크 |
| Database | MySQL | 관계형 데이터베이스 |
| ORM | Spring Data JPA | 데이터베이스 매핑 및 추상화 |
| Security | Spring Security, JWT | 인증/인가 처리 |
| Docs | Springdoc OpenAPI (Swagger) | API 명세 자동화, 프론트 협업용 |
| Build | Gradle | 의존성 관리 및 빌드 |
| Deploy | AWS EC2, AWS RDS | 애플리케이션 및 DB 배포 |

---

## 🚀 Getting Started

이 프로젝트는 **Java 17 이상** 환경을 권장합니다.

```bash
java -version
```

### 1. 프로젝트 클론

```bash
git clone https://github.com/UMCLearningLM/UMCLearningLM_BE.git
cd UMCLearningLM_BE
```

### 2. 환경 변수 설정

`src/main/resources/application.yml` 은 커밋하지 않으며, 로컬에서 아래 값을 설정합니다.
(팀 Notion/Discord에 공유된 값을 사용하세요.)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/learninglm
    username: { DB_USERNAME }
    password: { DB_PASSWORD }

jwt:
  secret: { JWT_SECRET }
```

### 3. 빌드 및 실행

```bash
./gradlew build
./gradlew bootRun
```

서버는 아래 주소에서 실행됩니다.

```
http://localhost:8080
```

### 4. API 문서 확인 (Swagger)

```
http://localhost:8080/swagger-ui/index.html
```

---

## 📂 Project Structure

도메인 중심 개발을 위해 도메인 기반 패키지 구조를 따릅니다.
프론트엔드의 `VITE_API_BASE_URL` 설정에 맞추어 모든 엔드포인트는 `/api` 하위 경로로 매핑됩니다.

```text
src/main/java/com/umc/learninglm/
├── global/                    # 전역 공용 설정 및 예외 처리
│   ├── config/                # Security, CORS, Swagger, JPA 설정
│   ├── error/                 # GlobalExceptionHandler, 공통 에러 코드
│   └── common/                # 공통 응답 포맷 (BaseResponse)
│
├── domain/                    # 핵심 도메인별 기능 모음
│   ├── auth/                  # 로그인, 회원가입, 토큰 처리
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   │
│   ├── home/                  # 홈 화면 데이터 제공
│   ├── tutorial/              # 공식 튜토리얼 및 단계별 가이드 API
│   ├── block/                 # 스튜디오 블록 팔레트 API
│   ├── flow/                  # 워크플로우(흐름) 생성/편집/검수/미리보기 API
│   ├── storage/               # My Storage (저장 워크플로우 관리)
│   └── library/               # Public Library (공개 워크플로우 탐색)
│
└── LearningLmApplication.java # 애플리케이션 시작점
```

### 개발 원칙

- **Domain First**: 기능은 도메인 폴더 내부에서 관리하고, 도메인 간 직접 참조를 최소화합니다.
- **Thin Controller**: Controller는 요청/응답만 담당하고, 비즈니스 로직은 Service에 둡니다.
- **공통 응답 포맷**: 모든 API 응답은 `BaseResponse<T>` 로 감싸서 반환합니다.
- **공통 예외 처리**: 예외는 `global/error` 의 `GlobalExceptionHandler` 와 공통 에러 코드로 처리합니다.

---

## 🧭 API 협업 원칙

프론트엔드 파트와의 원활한 협업을 위해 계약 중심(Contract-first) 개발을 지향합니다.

- **Swagger 기반 조기 계약**: Controller와 DTO 구조를 먼저 확정하여 Swagger 명세를 프론트에 우선 제공합니다. 프론트는 Mock 데이터로 병렬 개발이 가능합니다.
- **조기 Dev 배포**: CORS, HTTPS 환경 변수 이슈를 조기에 잡기 위해 AWS Dev 서버를 초기에 배포합니다.
- **Base URL 통일**: 모든 엔드포인트는 `/api` 로 시작합니다.

---

## 🌿 Branch Strategy

### 기본 브랜치

- `main`: 배포 가능한 안정 버전
- `dev`: 개발 통합 브랜치
- `feature/*`: 기능 개발 브랜치

### 브랜치 명명 규칙

`타입/기능명_작성자`

| 타입 | 설명 | 예시 |
| --- | --- | --- |
| feat | 새로운 기능 추가 | `feat/auth-jwt_hyunsu` |
| fix | 버그 수정 | `fix/flow-save_hyunsu` |
| refactor | 코드 구조 개선 | `refactor/base-response_hyunsu` |
| docs | 문서 수정 | `docs/readme_hyunsu` |
| chore | 빌드, 설정 변경 | `chore/gradle-deps_hyunsu` |

---

## ✅ Commit Convention

커밋 메시지는 Conventional Commits 규칙을 따릅니다.

```
feat: JWT 로그인 API 구현
fix: Flow 저장 시 stage_order 누락 수정
refactor: 공통 응답 포맷 BaseResponse 분리
docs: README 작성
chore: Gradle 의존성 정리
```

---

## 💻 Code Style

- **네이밍**: 클래스/인터페이스는 `PascalCase`, 메서드·변수는 `camelCase`, 상수는 `UPPER_SNAKE_CASE`.
- **들여쓰기**: 4 spaces, 탭 사용 금지. IDE 기본 포맷터(IntelliJ 기본값) 기준으로 커밋 전 정렬합니다.
- **패키지/클래스 네이밍**: `XxxController`, `XxxService`/`XxxServiceImpl`, `XxxRepository`, `XxxRequest`/`XxxResponse`, `Xxx`(Entity) 규칙을 따릅니다.
- **Lombok**: `@Getter`는 자유롭게 사용, `@Setter`는 지양(불변성 우선). `@Data`는 사용하지 않습니다(equals/hashCode/toString 의도치 않은 동작 방지). 생성자가 복잡한 경우에만 `@Builder`를 사용합니다.
- **import**: `*` 와일드카드 import 금지, 사용하지 않는 import는 커밋 전에 제거합니다.
- **매직 넘버/문자열**: 상수로 추출해서 사용합니다.
- **한 클래스 = 한 책임**: 파일당 public 클래스는 하나만 둡니다.

---

## 🔀 PR Rule

- `feature/*` → `dev` 로 PR을 올립니다. `main` 직접 푸시는 금지합니다.
- PR 전에 로컬에서 빌드가 통과하는지 확인합니다.

```bash
./gradlew build
```

- **리뷰어 지정**: PR 생성 시 팀원 중 최소 1명을 리뷰어로 지정합니다.
- **승인 조건**: 리뷰어 1명 이상의 Approve 없이는 머지하지 않습니다.
- **머지 방식**: `Squash and Merge`를 사용해 커밋 히스토리를 깔끔하게 유지합니다.
- **컨플릭트 처리**: 컨플릭트는 PR 작성자가 직접 해결한 뒤 다시 리뷰를 요청합니다.
- **PR 크기**: 하나의 PR은 하나의 기능/수정 단위로 작게 유지합니다(리뷰 용이성).

---

## 👀 Code Review Rule

- **리뷰 코멘트 접두사**: 코멘트 의도를 명확히 하기 위해 아래 접두사를 붙입니다.
  - `[must]` 머지 전 반드시 반영해야 하는 사항
  - `[suggest]` 반영을 권장하지만 필수는 아닌 제안
  - `[nit]` 사소한 스타일/오타 지적
  - `[question]` 의도 확인을 위한 질문
- **응답 원칙**: 리뷰 요청 후 가능한 한 24시간 이내에 확인/응답합니다.
- **반영 후 절차**: 리뷰 코멘트를 반영했다면 커밋 후 `Re-request review`로 재검토를 요청합니다.
- **리뷰 태도**: 코드에 대한 피드백이지 작성자에 대한 평가가 아님을 전제로, 이유를 함께 남깁니다.

---

## 🚢 Deployment

- `main` 브랜치 기준으로 AWS EC2에 프로덕션 배포를 진행합니다.
- DB는 AWS RDS(MySQL)를 사용합니다.

---

## 🛠 Troubleshooting

**Q. `./gradlew bootRun` 실행 시 DB 연결 에러가 발생해요.**
로컬 MySQL이 실행 중인지, `application.yml` 의 접속 정보가 맞는지 확인하세요.

**Q. Swagger 페이지가 열리지 않아요.**
서버가 정상 실행 중인지 확인 후 `http://localhost:8080/swagger-ui/index.html` 로 접속하세요.

**Q. 프론트에서 CORS 에러가 발생해요.**
`global/config` 의 CORS 설정에 프론트 개발 서버 주소(`http://localhost:5173`)가 포함되어 있는지 확인하세요.

---

## 문의

문의사항은 팀 Discord 또는 Notion에 공유된 백엔드 채널을 이용해 주세요.
