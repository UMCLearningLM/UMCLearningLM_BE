package com.umc.learninglm.domain.flow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "흐름 카테고리 정보")
public record FlowCategoryResponse(
		@Schema(example = "1") Long categoryId,
		@Schema(example = "RESEARCH") String code,
		@Schema(example = "자료조사") String name
) {
}
