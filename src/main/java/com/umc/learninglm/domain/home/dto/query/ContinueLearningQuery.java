package com.umc.learninglm.domain.home.dto.query;

import java.time.LocalDateTime;

public record ContinueLearningQuery(
        Long tutorialId,
        Long flowId,
        String title,
        String difficulty,
        Integer currentStepOrder,
        Long totalSteps,
        String status,
        String thumbnailUrl,
        LocalDateTime updatedAt
) {
}
