package com.umc.learninglm.domain.home.dto.query;

import java.time.LocalDateTime;

public record RecentTutorialQuery(
        Long tutorialId,
        Long flowId,
        String title,
        String difficulty,
        String status,
        String thumbnailUrl,
        LocalDateTime savedAt
) {
}