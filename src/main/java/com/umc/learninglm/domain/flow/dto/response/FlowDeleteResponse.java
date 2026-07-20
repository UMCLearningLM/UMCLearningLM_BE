package com.umc.learninglm.domain.flow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "흐름 삭제 응답")
public record FlowDeleteResponse(
		@Schema(example = "12") Long flowId,
		@Schema(example = "true") boolean deleted
) {
}
