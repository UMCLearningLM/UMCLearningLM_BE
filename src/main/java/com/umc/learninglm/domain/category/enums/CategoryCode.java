package com.umc.learninglm.domain.category.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// categories.name(코드값) → API 표시명(한글) 매핑. 한글명은 API 명세 예시 기준.
@Getter
@RequiredArgsConstructor
public enum CategoryCode {

	COMMUNITY("커뮤니티"),
	RESEARCH("자료조사"),
	TUTORIAL ("튜토리얼"),
	DOCUMENT_SUMMARY ("문서요약"),
	SUMMARY("요약"),
	WRITING("글쓰기"),
	REVIEW("결과물 검토"),
	AI_TOOL("AI 툴 활용"),
	ROUTINE("반복 작업 정리");

	private final String displayName;
}
