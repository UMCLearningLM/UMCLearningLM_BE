package com.umc.learninglm.domain.home.dto.query;

public record RecommendedTutorialQuery(
        Long tutorialId,
        String title,
        String summary,
        String difficulty,
        Long blockCount,
        Integer estimatedMinutes,
        String thumbnailUrl
) {
}
