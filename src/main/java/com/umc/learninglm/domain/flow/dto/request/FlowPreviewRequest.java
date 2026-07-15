package com.umc.learninglm.domain.flow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import java.util.List;

@Schema(description = "예시 결과 생성 요청")
public record FlowPreviewRequest(
		@Schema(description = "프롬프트 생성 기준이 되는 블록 배치 목록 (저장 전 상태 기준)")
		@Valid
		List<FlowBlockOptionRequest> blocks
) {
}
