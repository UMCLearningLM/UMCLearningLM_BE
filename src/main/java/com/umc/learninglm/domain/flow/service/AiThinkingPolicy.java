package com.umc.learninglm.domain.flow.service;

import com.umc.learninglm.domain.block.dto.prompt.CompiledPromptFragment;
import com.umc.learninglm.domain.block.enums.BlockType;
import com.umc.learninglm.domain.flow.dto.ai.AiModelConfiguration;
import com.umc.learninglm.domain.flow.dto.harness.CompiledAiHarness;
import com.umc.learninglm.domain.flow.enums.ThinkingProfile;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AiThinkingPolicy {

	private final int lowReasoningBlockCount;
	private final int balancedReasoningBlockCount;
	private final int lowPromptTokens;
	private final int balancedPromptTokens;
	private final int minimumOutputTokens;
	private final int maximumOutputTokens;
	private final double temperature;
	private final int offThinkingBudget;
	private final int lowThinkingBudget;
	private final int balancedThinkingBudget;

	public AiThinkingPolicy(
			@Value("${ai.thinking.low-reasoning-block-count:${AI_THINKING_LOW_BLOCK_COUNT:3}}")
			int lowReasoningBlockCount,
			@Value("${ai.thinking.balanced-reasoning-block-count:${AI_THINKING_BALANCED_BLOCK_COUNT:6}}")
			int balancedReasoningBlockCount,
			@Value("${ai.thinking.low-prompt-tokens:${AI_THINKING_LOW_PROMPT_TOKENS:2000}}")
			int lowPromptTokens,
			@Value("${ai.thinking.balanced-prompt-tokens:${AI_THINKING_BALANCED_PROMPT_TOKENS:4000}}")
			int balancedPromptTokens,
			@Value("${ai.generation.min-output-tokens:${AI_MIN_OUTPUT_TOKENS:1024}}")
			int minimumOutputTokens,
			@Value("${ai.generation.max-output-tokens:${AI_MAX_OUTPUT_TOKENS:8192}}")
			int maximumOutputTokens,
			@Value("${ai.generation.temperature:${AI_TEMPERATURE:0.2}}")
			double temperature,
			@Value("${ai.thinking.off-budget:${AI_THINKING_OFF_BUDGET:0}}")
			int offThinkingBudget,
			@Value("${ai.thinking.low-budget:${AI_THINKING_LOW_BUDGET:256}}")
			int lowThinkingBudget,
			@Value("${ai.thinking.balanced-budget:${AI_THINKING_BALANCED_BUDGET:512}}")
			int balancedThinkingBudget
	) {
		this.lowReasoningBlockCount = lowReasoningBlockCount;
		this.balancedReasoningBlockCount = balancedReasoningBlockCount;
		this.lowPromptTokens = lowPromptTokens;
		this.balancedPromptTokens = balancedPromptTokens;
		this.minimumOutputTokens = minimumOutputTokens;
		this.maximumOutputTokens = maximumOutputTokens;
		this.temperature = temperature;
		this.offThinkingBudget = offThinkingBudget;
		this.lowThinkingBudget = lowThinkingBudget;
		this.balancedThinkingBudget = balancedThinkingBudget;
		validateConfiguration();
	}

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
				minimumOutputTokens,
				Math.min(harness.estimatedOutputTokens(), maximumOutputTokens)
		);

		return new AiModelConfiguration(
				profile,
				resolveThinkingBudget(profile),
				maxOutputTokens,
				temperature
		);
	}

	private ThinkingProfile resolveProfile(
			long reasoningBlockCount,
			int estimatedInputTokens
	) {
		if (reasoningBlockCount >= balancedReasoningBlockCount
				|| estimatedInputTokens >= balancedPromptTokens) {
			return ThinkingProfile.BALANCED;
		}
		if (reasoningBlockCount >= lowReasoningBlockCount
				|| estimatedInputTokens >= lowPromptTokens) {
			return ThinkingProfile.LOW;
		}
		return ThinkingProfile.OFF;
	}

	private int resolveThinkingBudget(ThinkingProfile profile) {
		return switch (profile) {
			case OFF -> offThinkingBudget;
			case LOW -> lowThinkingBudget;
			case BALANCED -> balancedThinkingBudget;
		};
	}

	private void validateConfiguration() {
		if (lowReasoningBlockCount < 0
				|| balancedReasoningBlockCount < lowReasoningBlockCount
				|| lowPromptTokens < 0
				|| balancedPromptTokens < lowPromptTokens
				|| minimumOutputTokens <= 0
				|| maximumOutputTokens < minimumOutputTokens
				|| temperature < 0.0
				|| temperature > 2.0
				|| offThinkingBudget < 0
				|| lowThinkingBudget < 0
				|| balancedThinkingBudget < 0) {
			throw new IllegalArgumentException("AI 실행 환경변수 설정이 올바르지 않습니다.");
		}
	}
}