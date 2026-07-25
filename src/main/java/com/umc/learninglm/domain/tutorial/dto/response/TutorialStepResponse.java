package com.umc.learninglm.domain.tutorial.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "가이드 모드 단계")
public record TutorialStepResponse(
		@Schema(example = "1") Long stepId,
		@Schema(example = "1") Integer stepOrder,
		@Schema(example = "입력") String title,
		@Schema(example = "이 단계에 맞는 블록은 입력 그룹에 있습니다. 알맞은 블록을 골라 ...") String description,
		@Schema(description = "단계별 추천 블록 목록") List<TutorialStepBlockResponse> blocks
) {
}
