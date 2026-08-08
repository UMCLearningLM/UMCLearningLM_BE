package com.umc.learninglm.domain.flow.client;

import com.umc.learninglm.domain.flow.dto.ai.AiGenerationResult;
import com.umc.learninglm.domain.flow.dto.ai.AiModelConfiguration;
import com.umc.learninglm.domain.flow.dto.harness.CompiledAiHarness;

public interface AiModelClient {

	String modelName();

	AiGenerationResult generate(
			CompiledAiHarness harness,
			AiModelConfiguration configuration
	);
}
