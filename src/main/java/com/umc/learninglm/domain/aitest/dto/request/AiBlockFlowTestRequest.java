package com.umc.learninglm.domain.aitest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AiBlockFlowTestRequest(
		@NotBlank
		String request,

		@NotBlank
		String code,

		@NotBlank
		String projectContext,

		@NotBlank
		String officialGuidance
) {
}
