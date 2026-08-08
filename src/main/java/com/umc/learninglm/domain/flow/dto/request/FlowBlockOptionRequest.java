package com.umc.learninglm.domain.flow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.Map;

@Schema(description = "검증·예시 결과 생성 요청의 블록 배치 항목")
public record FlowBlockOptionRequest(
        @Schema(example = "3")
        @NotNull(message = "블록 식별자는 필수입니다.")
        Long blockId,

        @Schema(example = "1")
        @NotNull(message = "블록 순서는 필수입니다.")
        @Positive(message = "블록 순서는 1 이상이어야 합니다.")
        Integer blockOrder,

        @Schema(description = "블록이 처리할 입력값", nullable = true)
        Map<String, Object> input,

        @Schema(description = "블록별 옵션(optionSchema 기준)", nullable = true)
        Map<String, Object> options,

        @Schema(description = "파일 파싱·외부 조회 등 Flow에서 확정한 컨텍스트", nullable = true)
        Map<String, Object> resolvedContext
) {

    public FlowBlockOptionRequest(
            Long blockId,
            Integer blockOrder,
            Map<String, Object> options
    ) {
        this(blockId, blockOrder, Map.of(), options, Map.of());
    }
}