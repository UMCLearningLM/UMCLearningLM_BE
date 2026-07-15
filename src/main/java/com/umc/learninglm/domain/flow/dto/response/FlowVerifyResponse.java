package com.umc.learninglm.domain.flow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "블록 조합 검수 응답")
public record FlowVerifyResponse(
		@Schema(description = "전체 검수 결과", example = "FAIL") String totalStatus,
		@Schema(description = "규칙별 결과 집계") FlowVerifySummaryResponse summary,
		@Schema(description = "규칙별 상세 결과") List<FlowVerifyRuleResultResponse> results
) {
}
