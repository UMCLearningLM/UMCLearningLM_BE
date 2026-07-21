package com.umc.learninglm.domain.storage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 저장소 목록별 개수 (WF-17 배지용)")
public record StorageCountsResponse(
		@Schema(description = "저장한 튜토리얼 개수", example = "3") int saved,
		@Schema(description = "내가 만든 흐름 개수", example = "3") int own,
		@Schema(description = "복사한 흐름 개수", example = "2") int copied
) {
}
