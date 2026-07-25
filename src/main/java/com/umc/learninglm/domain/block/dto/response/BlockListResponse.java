package com.umc.learninglm.domain.block.dto.response;

import java.util.List;

public record BlockListResponse(
        String mode,
        Long tutorialId,
        List<StageResponse> stages
) {

    public record StageResponse(
            String stage,
            String label,
            List<BlockResponse> blocks
    ) {
    }

    public record BlockResponse(
            Long blockId,
            String name,
            String description,
            String status,
            Boolean required
    ) {
    }
}
