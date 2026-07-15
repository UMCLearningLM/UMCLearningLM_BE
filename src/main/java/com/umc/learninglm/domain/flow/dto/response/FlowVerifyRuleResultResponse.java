package com.umc.learninglm.domain.flow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "검수 규칙별 결과")
public record FlowVerifyRuleResultResponse(
		@Schema(example = "1") Long ruleId,
		@Schema(example = "입력 노드 CORE 블록") String name,
		@Schema(description = "검수 결과 상태", example = "PASS", allowableValues = {"PASS", "INSUFFICIENT", "PENDING"}) String status,
		@Schema(example = "입력 단계에 필수 블록 1개 이상이 포함되어야 합니다.") String criteria,
		@Schema(example = "\"텍스트 입력\" 블록이 포함되어 있습니다.") String checkedResult,
		@Schema(example = "검토 노드에서 기준(정확성·간결성)을 선택하세요.", nullable = true) String guide,
		@Schema(description = "FE 검증 패널 표시용 대상 단계", example = "INPUT",
				allowableValues = {"INPUT", "CONTEXT", "PROCESS", "REVIEW", "OUTPUT"}, nullable = true) String targetStage
) {
}
