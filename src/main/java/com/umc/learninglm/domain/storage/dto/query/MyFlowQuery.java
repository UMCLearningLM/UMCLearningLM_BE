package com.umc.learninglm.domain.storage.dto.query;

import java.time.LocalDateTime;

public record MyFlowQuery(
        Long flowId,
        String title,
        String summary,
        String difficulty,
        String mode,
        String visibility,
        String status,
        Long originFlowId,
        String originalAuthorNickname,
        LocalDateTime updatedAt
) {
}
