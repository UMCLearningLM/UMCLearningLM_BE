package com.umc.learninglm.domain.tutorial.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "가이드 모드 단계 + 추천 블록 조회 응답")
public record TutorialStepsResponse(
		@Schema(example = "1") Long tutorialId,
		@Schema(example = "AI로 자료 조사 흐름 만들기") String title,
		@Schema(example = "5") int totalSteps,
		@Schema(description = "로그인 사용자의 진행 정보 (없으면 null)", nullable = true) TutorialProgressResponse progress,
		@Schema(description = "단계 목록") List<TutorialStepResponse> steps
) {
}
