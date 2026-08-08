package com.umc.learninglm.domain.flow.dto.ai;

import com.umc.learninglm.domain.flow.enums.ThinkingProfile;

public record AiModelConfiguration(
		ThinkingProfile thinkingProfile,
		int thinkingBudget,
		int maxOutputTokens,
		double temperature
) {
}