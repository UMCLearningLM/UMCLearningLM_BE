package com.umc.learninglm.domain.storage.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "저장한 튜토리얼 목록 조회 응답")
public record StorageTutorialListResponse(
		@Schema(description = "저장한 튜토리얼 전체 개수", example = "3") long totalElements,
		@Schema(description = "저장한 튜토리얼 목록") List<StorageTutorialResponse> tutorials,
		@Schema(description = "내 저장소 목록별 개수") StorageCountsResponse counts
) {
}
