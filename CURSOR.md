# Cursor에서 이 저장소 사용하기

이 프로젝트에는 Cursor 프로젝트 룰이 포함되어 있어서, Cursor에서 열면 프로젝트 컨텍스트와
행동 원칙이 자동으로 적용됩니다.

1. 이 폴더를 Cursor로 엽니다.
2. [`.cursor/rules/project-guidelines.mdc`](.cursor/rules/project-guidelines.mdc) 룰이 `alwaysApply: true`로 커밋되어 있어 별도 설치가 필요 없습니다.
3. Cursor **Settings → Rules**(또는 프로젝트 룰 UI)에서 `project-guidelines`가 보이면 정상 적용된 것입니다.

## Claude Code vs Cursor

- **Claude Code**: [`CLAUDE.md`](CLAUDE.md)를 자동으로 읽습니다.
- **Cursor**: `.cursor/rules/` 안의 파일을 자동으로 읽습니다. `CLAUDE.md`는 읽지 않습니다.

두 파일은 같은 내용(프로젝트 컨텍스트 + 행동 원칙)을 담고 있습니다. 내용을 바꿀 때는 두 파일을 함께 갱신하세요.

## 출처

행동 원칙(Think Before Coding / Simplicity First / Surgical Changes / Goal-Driven Execution)은
Andrej Karpathy의 관찰을 정리한 아래 저장소를 참고해 이 프로젝트에 맞게 반영했습니다.

https://github.com/multica-ai/andrej-karpathy-skills
