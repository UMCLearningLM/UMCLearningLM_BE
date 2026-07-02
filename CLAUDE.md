# CLAUDE.md

Claude Code가 이 저장소에서 작업할 때 자동으로 읽는 컨텍스트 파일입니다.

## 프로젝트

LearningLM 백엔드. Java 17 + Spring Boot(Gradle), 도메인 기반(Domain-based) 아키텍처.
전체 컨벤션(Tech Stack, 브랜치 전략, 커밋 컨벤션, PR 규칙)은 [README.md](README.md) 참고.

## 빌드/실행

```bash
./gradlew build       # 빌드 (테스트 포함)
./gradlew bootRun      # 로컬 실행 (localhost:8080)
./gradlew compileJava   # 컴파일만 빠르게 확인
```

## 현재 구조

`src/main/java/com/umc/learninglm/`
- `global/` — 공통 응답 포맷(`BaseResponse`), 예외 처리(`GlobalExceptionHandler`, `ErrorCode`), Security/CORS/Swagger 설정
- `domain/{auth,home,tutorial,studio,storage,library}` — 도메인별 패키지. 현재는 `package-info.java`로 담당 화면(WF 번호)만 표시된 뼈대 상태이며, 실제 구현은 팀원별로 진행 예정.

새 도메인 로직을 추가할 때는 `controller/service/repository/entity/dto` 서브패키지 구조를 따르고, 응답은 `BaseResponse<T>`로 감싼다.

---

## 행동 원칙 (Behavioral Guidelines)

Andrej Karpathy가 지적한 LLM 코딩 실수(근거 없는 가정, 과도한 추상화, 무관한 코드 수정, 불명확한 완료 기준)를 줄이기 위한 4가지 원칙.
출처: https://github.com/multica-ai/andrej-karpathy-skills

### 1. Think Before Coding

**가정하지 말 것. 혼란을 숨기지 말 것. 트레이드오프를 드러낼 것.**

- 가정은 명시적으로 말한다. 확신이 없으면 묻는다.
- 여러 해석이 가능하면 조용히 하나를 고르지 말고 제시한다.
- 더 단순한 방법이 있으면 말한다.
- 불명확하면 멈추고, 무엇이 불명확한지 짚어서 묻는다.

### 2. Simplicity First

**문제를 푸는 최소한의 코드. 추측성 코드는 넣지 않는다.**

- 요청받지 않은 기능을 넣지 않는다.
- 한 번만 쓰이는 코드에 추상화를 만들지 않는다.
- 요청받지 않은 "유연성"이나 "설정 가능성"을 넣지 않는다.
- 일어날 수 없는 상황에 대한 예외 처리를 넣지 않는다.
- 200줄이 50줄이 될 수 있다면 다시 쓴다.

### 3. Surgical Changes

**꼭 필요한 부분만 건드린다. 내가 만든 부산물만 정리한다.**

- 기존 코드를 수정할 때 인접한 코드/주석/포맷을 "개선"하지 않는다.
- 깨지지 않은 것을 리팩토링하지 않는다.
- 기존 스타일을 따른다(내 취향과 달라도).
- 관련 없는 죽은 코드를 발견하면 언급만 하고 삭제하지 않는다.
- 내 변경으로 안 쓰이게 된 import/변수/함수만 정리한다.

### 4. Goal-Driven Execution

**성공 기준을 정의하고, 검증될 때까지 반복한다.**

- "검증 추가해줘" → "잘못된 입력에 대한 테스트를 작성하고 통과시켜줘"
- "버그 고쳐줘" → "버그를 재현하는 테스트를 작성하고 통과시켜줘"
- "X 리팩토링해줘" → "리팩토링 전후로 테스트가 통과하는지 확인해줘"

여러 단계로 이루어진 작업은 간단한 계획을 먼저 제시한다:
```
1. [단계] → 검증: [확인 방법]
2. [단계] → 검증: [확인 방법]
```

**트레이드오프**: 이 원칙들은 속도보다 신중함에 기웁니다. 오타 수정처럼 사소한 작업에는 판단해서 유연하게 적용하세요.
