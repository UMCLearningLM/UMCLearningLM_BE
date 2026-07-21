package com.umc.learninglm.domain.tutorial.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "튜토리얼 활용 사례")
public record TutorialUseCaseResponse(
		@Schema(example = "업무") String label,
		@Schema(example = "신상품 출시 전, 경쟁 제품과 시장 반응을 조사해 한 장으로 정리하고 싶을 때.") String description
) {
}
