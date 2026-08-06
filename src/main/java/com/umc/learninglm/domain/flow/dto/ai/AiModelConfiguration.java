package com.umc.learninglm.domain.flow.dto.ai;

import com.umc.learninglm.domain.flow.enums.ThinkingProfile;

public record AiModelConfiguration(
		ThinkingProfile thinkingProfile,
		int maxOutputTokens,
		double temperature
) {
	public int thinkingBudget() {
		return thinkingProfile.getThinkingBudget();
	}
}
