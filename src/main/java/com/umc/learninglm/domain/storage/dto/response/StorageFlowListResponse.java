package com.umc.learninglm.domain.storage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "내가 만든/복사한 흐름 목록 조회 응답")
public record StorageFlowListResponse(
		@Schema(description = "흐름 전체 개수", example = "3") long totalElements,
		@Schema(description = "흐름 목록") List<StorageFlowResponse> flows,
		@Schema(description = "내 저장소 목록별 개수") StorageCountsResponse counts
) {
}
