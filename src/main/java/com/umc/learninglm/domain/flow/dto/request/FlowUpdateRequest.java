package com.umc.learninglm.domain.flow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "흐름 저장(전체 갱신) 요청")
public record FlowUpdateRequest(
		@Schema(example = "제품 리뷰 요약기")
		@NotBlank(message = "제목은 필수입니다.")
		String title,

		@Schema(example = "리뷰 더미에서 장단점을 추출", nullable = true)
		String summary,

		@Schema(example = "여러 리뷰를 비교해 핵심만 정리", nullable = true)
		String purpose,

		@Schema(description = "난이도 코드", example = "BASIC", allowableValues = {"BEGINNER", "BASIC", "ADVANCED"}, nullable = true)
		String difficulty,

		@Schema(example = "1")
		@NotNull(message = "카테고리 식별자는 필수입니다.")
		Long categoryId,

		@Schema(description = "공개 범위", example = "PRIVATE", allowableValues = {"PRIVATE", "PUBLIC"})
		@NotBlank(message = "공개 범위는 필수입니다.")
		String visibility,

		@Schema(example = "COMPLETED", allowableValues = {"DRAFT", "COMPLETED"})
		@NotBlank(message = "상태는 필수입니다.")
		String status,

		@Schema(example = "검색 블록 기간을 좁히면 정확도가 올라갑니다.", nullable = true)
		String authorNote,

		@Schema(example = "리뷰 100건을 항목별로 정리해줘", nullable = true)
		String exampleInput,

		@Schema(example = "비교 표 예시", nullable = true)
		String exampleResult,

		@Schema(description = "흐름 유형", example = "USER")
		@NotBlank(message = "흐름 유형은 필수입니다.")
		String flowType,

		@Schema(description = "생성 모드", example = "GUIDED", allowableValues = {"GUIDED", "CREATE"})
		@NotBlank(message = "모드는 필수입니다.")
		String mode,

		@Schema(description = "블록 배치 목록 (전체 교체)")
		@NotNull(message = "블록 배치 목록은 필수입니다.")
		@Valid
		List<FlowBlockRequest> blocks
) {
}