package com.umc.learninglm.domain.home.dto.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.LocalDateTime;
import java.util.List;

public record HomeResponse(
        boolean isGuest,
        ContinueLearningResponse continueLearning,
        List<CategoryResponse> categories,
        List<RecommendedTutorialResponse> recommendedTutorials,
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
            CategoryResponse category,
            List<String> tags,
            int blockCount,
            int estimatedMinutes,
            String thumbnailUrl
    ) {
    }

    // 인기 공개 흐름
    public record PopularFlowResponse(
            Long flowId,
            String title,
            String summary,
            String difficulty,
            CategoryResponse category,
            AuthorResponse author,
            long likeCount,
            long copyCount,
            long commentCount
    ) {
    }

    public record AuthorResponse(
            Long userId,
            String nickname
    ) {
    }

    // 최근 저장 항목 TUTORIAL / COPIED_FLOW 구분
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXISTING_PROPERTY,
            property = "itemType",
            visible = true
    )
    @JsonSubTypes({
            @JsonSubTypes.Type(
                    value = TutorialSavedItemResponse.class,
                    name = "TUTORIAL"
            ),
            @JsonSubTypes.Type(
                    value = CopiedFlowSavedItemResponse.class,
                    name = "COPIED_FLOW"
            )
    })
    public sealed interface RecentSavedItemResponse
            permits TutorialSavedItemResponse, CopiedFlowSavedItemResponse {

        String itemType();

        LocalDateTime updatedAt();
    }

    public record TutorialSavedItemResponse(
            String itemType,
            Long tutorialId,
            Long flowId,
            String title,
            String difficulty,
            String status,
            int currentStepOrder,
            int totalSteps,
            int progressRate,
            String thumbnailUrl,
            LocalDateTime updatedAt
    ) implements RecentSavedItemResponse {
    }

    public record CopiedFlowSavedItemResponse(
            String itemType,
            Long flowId,
            Long originFlowId,
            String title,
            String difficulty,
            CategoryResponse category,
            AuthorResponse originalAuthor,
            LocalDateTime updatedAt
    ) implements RecentSavedItemResponse {
    }
}