package com.umc.learninglm.domain.tutorial.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "튜토리얼 학습 시작 응답")
public record TutorialProgressStartResponse(
		@Schema(example = "1") Long tutorialId,
		@Schema(example = "1") int currentStepOrder,
		@Schema(example = "5") int totalSteps,
		@Schema(description = "진행률(%)", example = "0") int progressRate,
		@Schema(description = "진행 상태", example = "IN_PROGRESS", allowableValues = {"NOT_STARTED", "IN_PROGRESS", "COMPLETED"}) String status,
		@Schema(example = "77") Long flowId,
		@Schema(example = "2026-07-05T14:05:00") LocalDateTime startedAt
) {
}
