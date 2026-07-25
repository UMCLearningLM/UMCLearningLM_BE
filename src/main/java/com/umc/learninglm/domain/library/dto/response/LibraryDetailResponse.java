package com.umc.learninglm.domain.library.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

// 공개 활용 흐름 상세 조회 응답.
public record LibraryDetailResponse(
        Long flowId,
        String title,
        String summary,
        String purpose,
        String difficulty,
        String flowType,
        CategoryResponse category,
        AuthorResponse author,
        List<TagResponse> tags,
        List<FlowBlockResponse> blockFlow,
        String exampleInput,
        String exampleResult,
        String exampleResultSource,
        String authorNote,
        long likeCount,
        long copyCount,
        long bookmarkCount,
        long commentCount,
        boolean isLiked,
        boolean isBookmarked,
        List<CommentResponse> comments,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public record CategoryResponse(
            Long categoryId,
            String code,
            String name
    ) {
    }

    public record AuthorResponse(
            Long userId,
            String nickname
    ) {
    }

    public record TagResponse(
            Long tagId,
            String name
    ) {
    }

    public record FlowBlockResponse(
            Long flowBlockId,
            Long blockId,
            String name,
            String stage,
            int blockOrder,
            Map<String, Object> options,
            Object inputValue,
            Object outputValue,
            String executionMode,
            Long promptTemplateId
    ) {
    }

    public record CommentResponse(
            Long commentId,
            AuthorResponse author,
            String content,
            LocalDateTime createdAt
    ) {
    }
}