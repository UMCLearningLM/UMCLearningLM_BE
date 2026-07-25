package com.umc.learninglm.domain.tutorial.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 사용자의 튜토리얼 진행 정보")
public record TutorialProgressResponse(
		@Schema(example = "3") Integer currentStepOrder,
		@Schema(description = "진행 상태", example = "IN_PROGRESS", allowableValues = {"NOT_STARTED", "IN_PROGRESS", "COMPLETED"}) String status,
		@Schema(example = "77") Long flowId
) {
}
