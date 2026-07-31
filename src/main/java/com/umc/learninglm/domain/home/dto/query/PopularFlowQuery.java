package com.umc.learninglm.domain.home.dto.query;

public record PopularFlowQuery(
        Long flowId,
        String title,
        String summary,
        String difficulty,
        Long authorId,
        String authorNickname,
        Long likeCount,
        Long copyCount,
        Long commentCount
) {
}
