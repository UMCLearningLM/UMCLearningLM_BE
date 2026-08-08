package com.umc.learninglm.domain.tutorial.service;

import com.umc.learninglm.domain.tutorial.enums.SavedTutorialStatus;

// 튜토리얼 진행률 계산 정책. 튜토리얼·저장소·홈이 같은 값을 반환하도록 한 곳에서만 계산한다.
public final class TutorialProgressCalculator {

	private TutorialProgressCalculator() {
	}

	// 현재 진행 중인 단계의 이전 단계까지 완료한 것으로 본다.
	public static int completedStepCount(int currentStepOrder) {
		return Math.max(currentStepOrder - 1, 0);
	}

	// COMPLETED면 100, 그 외 round(완료 단계 수 / 전체 단계 수 * 100)
	public static int progressRate(SavedTutorialStatus status, int currentStepOrder, int totalSteps) {
		if (status == SavedTutorialStatus.COMPLETED) {
			return 100;
		}
		if (totalSteps <= 0) {
			return 0;
		}
		return (int) Math.round(completedStepCount(currentStepOrder) * 100.0 / totalSteps);
	}
}
