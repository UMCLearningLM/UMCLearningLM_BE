package com.umc.learninglm.domain.flow.service;

import com.umc.learninglm.domain.block.dto.prompt.CompiledPromptFragment;
import com.umc.learninglm.domain.block.enums.BlockType;
import com.umc.learninglm.domain.flow.dto.ai.AiModelConfiguration;
import com.umc.learninglm.domain.flow.dto.harness.CompiledAiHarness;
import com.umc.learninglm.domain.flow.enums.ThinkingProfile;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AiThinkingPolicy {

	private static final int LOW_REASONING_BLOCK_COUNT = 3;
	private static final int BALANCED_REASONING_BLOCK_COUNT = 6;
	private static final int LOW_PROMPT_TOKENS = 2000;
	private static final int BALANCED_PROMPT_TOKENS = 4000;
	private static final int MIN_OUTPUT_TOKENS = 1024;
	private static final int MAX_OUTPUT_TOKENS = 8192;
	private static final double DEFAULT_TEMPERATURE = 0.2;

	public AiModelConfiguration resolve(
			CompiledAiHarness harness,
			List<CompiledPromptFragment> fragments
	) {
		long reasoningBlockCount = fragments.stream()
				.filter(fragment -> fragment.fragment().stage() == BlockType.PROCESS
						|| fragment.fragment().stage() == BlockType.REVIEW)
				.count();
		ThinkingProfile profile = resolveProfile(
				reasoningBlockCount,
				harness.estimatedInputTokens()
		);
		int maxOutputTokens = Math.max(
				MIN_OUTPUT_TOKENS,
				Math.min(harness.estimatedOutputTokens(), MAX_OUTPUT_TOKENS)
		);

		return new AiModelConfiguration(
				profile,
				maxOutputTokens,
				DEFAULT_TEMPERATURE
		);
	}

	private ThinkingProfile resolveProfile(
			long reasoningBlockCount,
			int estimatedInputTokens
	) {
		if (reasoningBlockCount >= BALANCED_REASONING_BLOCK_COUNT
				|| estimatedInputTokens >= BALANCED_PROMPT_TOKENS) {
			return ThinkingProfile.BALANCED;
		}
		if (reasoningBlockCount >= LOW_REASONING_BLOCK_COUNT
				|| estimatedInputTokens >= LOW_PROMPT_TOKENS) {
			return ThinkingProfile.LOW;
		}
		return ThinkingProfile.OFF;
	}
}
