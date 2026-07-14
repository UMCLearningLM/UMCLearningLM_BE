package com.umc.learninglm.domain.tutorial.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "튜토리얼 상세 조회 응답")
public record TutorialDetailResponse(
		@Schema(example = "1") Long tutorialId,
		@Schema(example = "AI로 자료 조사 흐름 만들기") String title,
		@Schema(example = "검색·요약·정리 블록을 연결해 리서치 결과를 한눈에 정리하는 기본 흐름을 만들어봅니다.") String summary,
		@Schema(description = "난이도 코드", example = "BEGINNER", allowableValues = {"BEGINNER", "BASIC", "ADVANCED"}) String difficulty,
		@Schema(description = "카테고리 정보") TutorialCategoryResponse category,
		@Schema(description = "튜토리얼에 포함된 블록 수", example = "4") int blockCount,
		@Schema(example = "15") Integer estimatedMinutes,
		@Schema(description = "활용 사례 목록") List<TutorialUseCaseResponse> useCases,
		@Schema(description = "필요 개념 목록") List<String> requiredConcepts,
		@Schema(description = "실행 모드", example = "PRESET") String executionMode,
		@Schema(description = "블록 흐름 이름 목록 (순서대로)") List<String> blockFlow,
		@Schema(description = "블록 상세 목록") List<TutorialBlockResponse> blocks,
		@Schema(description = "예시 입력/결과") TutorialExampleResponse example,
		@Schema(description = "로그인 사용자의 저장 여부 (비로그인 시 false)", example = "false") boolean saved
) {
}
