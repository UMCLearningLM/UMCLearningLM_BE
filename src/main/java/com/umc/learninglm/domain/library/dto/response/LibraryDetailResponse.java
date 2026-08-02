package com.umc.learninglm.domain.library.dto.response;

import java.time.LocalDateTime;
import java.util.List;

// 공개 활용 흐름 상세 조회 응답
public record LibraryDetailResponse(
        Long flowId,
        String title,
        String summary,
        String difficulty,
        List<CategoryResponse> categories,
        AuthorResponse author,
        List<TagResponse> tags,
        List<FlowBlockResponse> blockFlow,
        String exampleInput,
        String exampleResult,
        String authorNote,
        long likeCount,
        long copyCount,
        long bookmarkCount,
        long commentCount,
        boolean isLiked,
        boolean isBookmarked,
        List<CommentResponse> comments
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
            int blockOrder
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