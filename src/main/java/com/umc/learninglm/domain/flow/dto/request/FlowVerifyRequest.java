package com.umc.learninglm.domain.flow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import java.util.List;

@Schema(description = "블록 조합 검수 요청")
public record FlowVerifyRequest(
		@Schema(description = "검수 대상 블록 배치 목록 (저장 전 상태 기준, 0개면 전체 PENDING으로 응답)")
		@Valid
		List<FlowBlockOptionRequest> blocks
) {
}
