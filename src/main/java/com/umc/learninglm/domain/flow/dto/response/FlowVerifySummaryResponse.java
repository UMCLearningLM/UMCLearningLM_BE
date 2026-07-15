package com.umc.learninglm.domain.flow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "검수 결과 집계")
public record FlowVerifySummaryResponse(
		@Schema(example = "3") int pass,
		@Schema(example = "1") int insufficient,
		@Schema(example = "1") int pending
) {
}
