package com.umc.learninglm.domain.home.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record HomeResponse(
        boolean isGuest,
        ContinueLearningResponse continueLearning,
        List<RecommendedTutorialResponse> recommendedTutorials,
        List<CategoryResponse> categories,
        List<PopularFlowResponse> popularFlows,
        List<RecentSavedItemResponse> recentSavedItems
) {

    // 이어서 학습하기 (비회원인 경우 null)
    public record ContinueLearningResponse(
            Long tutorialId,
            Long flowId,
            String title,
            String difficulty,
            int currentStepOrder,
            int totalSteps,
            int completedStepCount,
            int progressRate,
            String status,
            String thumbnailUrl,
            LocalDateTime updatedAt
    ) {
    }

    // 카테고리 정보
    public record CategoryResponse(
            Long categoryId,
            String code,
            String name
    ) {
    }

    // 추천 튜토리얼
    public record RecommendedTutorialResponse(
            Long tutorialId,
            String title,
            String summary,
            String difficulty,
            List<CategoryResponse> categories,
            int blockCount,
            Integer estimatedMinutes,
            String thumbnailUrl
    ) {
    }

    // 인기 공개 흐름
    public record PopularFlowResponse(
            Long flowId,
            String title,
            String summary,
            String difficulty,
            List<CategoryResponse> categories,
            AuthorResponse author,
            Long likeCount,
            Long copyCount,
            Long commentCount
    ) {
    }

    public record AuthorResponse(
            Long userId,
            String nickname
    ) {
    }

    // 최근 저장 항목 TUTORIAL / COPIED_FLOW 구분
    public sealed interface RecentSavedItemResponse
            permits TutorialSavedItemResponse, CopiedFlowSavedItemResponse {

        String itemType();

        LocalDateTime savedAt();
    }

    public record TutorialSavedItemResponse(
            String itemType,
            Long tutorialId,
            Long flowId,
            String title,
            String difficulty,
            String status,
            String thumbnailUrl,
            LocalDateTime savedAt
    ) implements RecentSavedItemResponse {

        public TutorialSavedItemResponse(
                Long tutorialId,
                Long flowId,
                String title,
                String difficulty,
                String status,
                String thumbnailUrl,
                LocalDateTime savedAt
        ) {
            this(
                    "TUTORIAL",
                    tutorialId,
                    flowId,
                    title,
                    difficulty,
                    status,
                    thumbnailUrl,
                    savedAt
            );
        }
    }

    public record CopiedFlowSavedItemResponse(
            String itemType,
            Long flowId,
            Long originFlowId,
            String title,
            String difficulty,
            AuthorResponse originalAuthor,
            String thumbnailUrl,
            LocalDateTime savedAt
    ) implements RecentSavedItemResponse {

        public CopiedFlowSavedItemResponse(
                Long flowId,
                Long originFlowId,
                String title,
                String difficulty,
                AuthorResponse originalAuthor,
                String thumbnailUrl,
                LocalDateTime savedAt
        ) {
            this(
                    "COPIED_FLOW",
                    flowId,
                    originFlowId,
                    title,
                    difficulty,
                    originalAuthor,
                    thumbnailUrl,
                    savedAt
            );
        }
    }
}