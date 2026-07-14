package com.umc.learninglm.domain.tutorial.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "튜토리얼 예시 입력/결과")
public record TutorialExampleResponse(
		@Schema(example = "2025년 국내 전기차 시장 동향을 조사하고 핵심만 정리해줘") String input,
		@Schema(example = "…예시 결과 본문…") String result,
		@Schema(description = "예시 출처", example = "TEMPLATE") String source
) {
}
