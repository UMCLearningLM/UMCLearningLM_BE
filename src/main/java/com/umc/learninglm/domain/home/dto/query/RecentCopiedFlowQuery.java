package com.umc.learninglm.domain.home.dto.query;

import java.time.LocalDateTime;

public record RecentCopiedFlowQuery(
        Long flowId,
        Long originFlowId,
        String title,
        String difficulty,
        Long originalAuthorId,
        String originalAuthorNickname,
        LocalDateTime savedAt
) {
}
