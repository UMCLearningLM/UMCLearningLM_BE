package com.umc.learninglm.domain.flow.dto.harness;

import java.util.List;
import java.util.Map;

public record CompiledAiHarness(
		String systemInstruction,
		String prompt,
		Map<String, Object> responseSchema,
		List<LocalPostAction> postActions,
		int estimatedInputTokens,
		int estimatedOutputTokens
) {
}
