package com.umc.learninglm.domain.flow.enums;

public enum ThinkingProfile {
	OFF(0),
	LOW(256),
	BALANCED(512);

	private final int thinkingBudget;

	ThinkingProfile(int thinkingBudget) {
		this.thinkingBudget = thinkingBudget;
	}

	public int getThinkingBudget() {
		return thinkingBudget;
	}
}
