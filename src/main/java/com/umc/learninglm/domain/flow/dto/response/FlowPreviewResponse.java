package com.umc.learninglm.domain.flow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예시 결과 생성 응답")
public record FlowPreviewResponse(
		@Schema(example = "리뷰 100건 분석 결과, 장점은 …") String resultText,
		@Schema(description = "결과 출처 (AI: 생성 성공, TEMPLATE: Fallback)", example = "AI", allowableValues = {"AI", "TEMPLATE"}) String resultSource,
		@Schema(description = "TEMPLATE이면 null", example = "gemini-2.0-flash", nullable = true) String modelName
) {
}
