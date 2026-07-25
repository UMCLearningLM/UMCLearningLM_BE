package com.umc.learninglm.domain.library.dto.response;

import java.time.LocalDateTime;
import java.util.List;

// 공개 활용 흐름 목록 조회 응답.
public record LibraryListResponse(
        long totalElements,
        List<LibraryItemResponse> items
) {

    public record LibraryItemResponse(
            Long flowId,
            String title,
            String summary,
            String difficulty,
            CategoryResponse category,
            AuthorResponse author,
            List<TagResponse> tags,
            long likeCount,
            long copyCount,
            long commentCount,
            long bookmarkCount,
            boolean isLiked,
            boolean isBookmarked,
            LocalDateTime createdAt
    ) {
    }

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
}