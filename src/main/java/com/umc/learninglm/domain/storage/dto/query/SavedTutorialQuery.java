package com.umc.learninglm.domain.storage.dto.query;

import java.time.LocalDateTime;

public record SavedTutorialQuery(
        Long tutorialId,
        Long flowId,
        String title,
        String summary,
        String difficulty,
        String thumbnailUrl,
        String status,
        Integer currentStepOrder,
        Long totalSteps,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
