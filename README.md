# 📗 LearningLM Backend

AI를 단순 질문/답변 도구에서 실제 작업 흐름을 설계하는 도구로 확장하는 학습 플랫폼

LearningLM의 백엔드 저장소입니다.
React/TypeScript 기반 프론트엔드와 통신하며, 튜토리얼·Studio 워크플로우·저장/공유 기능을 위한 REST API를 제공합니다.
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
│   ├── studio/                # 워크플로우 제작 Studio API
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
| fix | 버그 수정 | `fix/studio-save_hyunsu` |
| refactor | 코드 구조 개선 | `refactor/base-response_hyunsu` |
| docs | 문서 수정 | `docs/readme_hyunsu` |
| chore | 빌드, 설정 변경 | `chore/gradle-deps_hyunsu` |

---

## ✅ Commit Convention

커밋 메시지는 Conventional Commits 규칙을 따릅니다.

```
feat: JWT 로그인 API 구현
fix: Studio 저장 시 stage_order 누락 수정
refactor: 공통 응답 포맷 BaseResponse 분리
docs: README 작성
chore: Gradle 의존성 정리
```

---

## 🔀 PR Rule

- `feature/*` → `dev` 로 PR을 올립니다. `main` 직접 푸시는 금지합니다.
- PR 전에 로컬에서 빌드가 통과하는지 확인합니다.

```bash
./gradlew build
```

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
