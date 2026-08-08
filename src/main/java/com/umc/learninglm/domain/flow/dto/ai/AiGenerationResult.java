package com.umc.learninglm.domain.flow.dto.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.umc.learninglm.domain.flow.enums.ThinkingProfile;

public record AiGenerationResult(
		String model,
		String text,
		JsonNode structuredOutput,
		int promptTokenCount,
		int candidatesTokenCount,
		int thoughtsTokenCount,
		int totalTokenCount,
		long durationMs,
		String finishReason,
		ThinkingProfile thinkingProfile
) {
}
