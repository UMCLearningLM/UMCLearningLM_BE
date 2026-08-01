package com.umc.learninglm.domain.library.service;

import com.umc.learninglm.domain.auth.entity.User;
import com.umc.learninglm.domain.auth.repository.UserRepository;
import com.umc.learninglm.domain.category.enums.CategoryCode;
import com.umc.learninglm.domain.library.dto.response.LibraryDetailResponse;
import com.umc.learninglm.domain.library.dto.response.LibraryListResponse;
import com.umc.learninglm.domain.library.repository.LibraryQueryRepository;
import com.umc.learninglm.domain.library.repository.LibraryQueryRepository.CategoryRow;
import com.umc.learninglm.domain.library.repository.LibraryQueryRepository.FlowDetailRow;
import com.umc.learninglm.domain.library.repository.LibraryQueryRepository.FlowSummaryRow;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LibraryService {

    private static final int MAX_CATEGORY_COUNT = 3;
    private static final int MAX_DIFFICULTY_COUNT = 2;

    private static final Set<String> DIFFICULTY_CODES = Set.of(
            "BEGINNER",
            "BASIC",
            "ADVANCED"
    );

    private final LibraryQueryRepository libraryQueryRepository;
    private final UserRepository userRepository;

    // 검색 및 필터 조건에 맞는 공개 흐름 전체를 반환
    public LibraryListResponse getLibraryFlows(
            String q,
            List<Long> categoryIds,
            List<String> difficulties
    ) {
        String keyword = normalizeKeyword(q);

        List<Long> normalizedCategoryIds =
                normalizeCategoryIds(categoryIds);

        List<String> normalizedDifficulties =
                normalizeDifficulties(difficulties);

        validateExistingCategories(normalizedCategoryIds);

        Long userId = currentUserIdOrNull();

        List<FlowSummaryRow> rows =
                libraryQueryRepository.findPublicFlows(
                        keyword,
                        normalizedCategoryIds,
                        normalizedDifficulties,
                        userId
                );

        if (rows.isEmpty()) {
            return new LibraryListResponse(
                    0,
                    List.of()
            );
        }

        List<Long> flowIds = rows.stream()
                .map(FlowSummaryRow::flowId)
                .toList();

        Map<Long, List<LibraryListResponse.CategoryResponse>>
                categoryMap = groupListCategories(
                libraryQueryRepository
                        .findCategoriesByFlowIds(flowIds)
        );

        List<LibraryListResponse.LibraryItemResponse> items =
                rows.stream()
                        .map(row ->
                                new LibraryListResponse.LibraryItemResponse(
                                        row.flowId(),
                                        row.title(),
                                        row.summary(),
                                        row.difficulty(),
                                        categoryMap.getOrDefault(
                                                row.flowId(),
                                                List.of()
                                        ),
                                        new LibraryListResponse.AuthorResponse(
                                                row.authorId(),
                                                row.authorNickname()
                                        ),
                                        row.likeCount(),
                                        row.copyCount(),
                                        row.commentCount(),
                                        row.isLiked()
                                )
                        )
                        .toList();

        return new LibraryListResponse(
                items.size(),
                items
        );
    }

    // 공개 흐름의 상세 정보와 최신 댓글을 반환
    public LibraryDetailResponse getLibraryFlowDetail(Long flowId) {
        Long userId = currentUserIdOrNull();

        FlowDetailRow flow =
                libraryQueryRepository
                        .findPublicFlowDetail(flowId, userId)
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.LIBRARY_FLOW_NOT_FOUND
                                )
                        );

        List<LibraryDetailResponse.CategoryResponse> categories =
                libraryQueryRepository
                        .findCategoriesByFlowIds(List.of(flowId))
                        .stream()
                        .map(this::toDetailCategoryResponse)
                        .toList();

        List<LibraryDetailResponse.TagResponse> tags =
                libraryQueryRepository
                        .findTagsByFlowId(flowId)
                        .stream()
                        .map(row ->
                                new LibraryDetailResponse.TagResponse(
                                        row.tagId(),
                                        row.name()
                                )
                        )
                        .toList();

        List<LibraryDetailResponse.FlowBlockResponse> blocks =
                libraryQueryRepository
                        .findBlocksByFlowId(flowId)
                        .stream()
                        .map(row ->
                                new LibraryDetailResponse.FlowBlockResponse(
                                        row.flowBlockId(),
                                        row.blockId(),
                                        row.name(),
                                        row.stage(),
                                        row.blockOrder()
                                )
                        )
                        .toList();

        List<LibraryDetailResponse.CommentResponse> comments =
                libraryQueryRepository
                        .findActiveCommentsByFlowId(flowId)
                        .stream()
                        .map(row ->
                                new LibraryDetailResponse.CommentResponse(
                                        row.commentId(),
                                        new LibraryDetailResponse.AuthorResponse(
                                                row.userId(),
                                                row.nickname()
                                        ),
                                        row.content(),
                                        row.createdAt()
                                )
                        )
                        .toList();

        return new LibraryDetailResponse(
                flow.flowId(),
                flow.title(),
                flow.summary(),
                flow.difficulty(),
                categories,
                new LibraryDetailResponse.AuthorResponse(
                        flow.authorId(),
                        flow.authorNickname()
                ),
                tags,
                blocks,
                flow.exampleInput(),
                flow.exampleResult(),
                flow.authorNote(),
                flow.likeCount(),
                flow.copyCount(),
                flow.bookmarkCount(),
                flow.commentCount(),
                flow.isLiked(),
                flow.isBookmarked(),
                comments
        );
    }

    // 검색어 앞뒤 공백을 제거하고 빈 문자열은 조건에서 제외
    private String normalizeKeyword(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }

        return q.trim();
    }

    // 카테고리 식별자를 중복 제거하고 최대 개수를 검증
    private List<Long> normalizeCategoryIds(
            List<Long> categoryIds
    ) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }

        if (categoryIds.stream().anyMatch(
                categoryId ->
                        categoryId == null || categoryId <= 0
        )) {
            throw new CustomException(
                    ErrorCode.LIBRARY_INVALID_FILTER
            );
        }

        List<Long> normalized =
                new ArrayList<>(
                        new LinkedHashSet<>(categoryIds)
                );

        if (normalized.size() > MAX_CATEGORY_COUNT) {
            throw new CustomException(
                    ErrorCode.LIBRARY_CATEGORY_LIMIT_EXCEEDED
            );
        }

        return normalized;
    }

    // 난이도 코드를 대문자로 변환하고 최대 개수를 검증
    private List<String> normalizeDifficulties(
            List<String> difficulties
    ) {
        if (difficulties == null || difficulties.isEmpty()) {
            return List.of();
        }

        if (difficulties.stream().anyMatch(
                difficulty ->
                        difficulty == null || difficulty.isBlank()
        )) {
            throw new CustomException(
                    ErrorCode.LIBRARY_INVALID_FILTER
            );
        }

        List<String> normalized =
                difficulties.stream()
                        .map(String::trim)
                        .map(value ->
                                value.toUpperCase(Locale.ROOT)
                        )
                        .distinct()
                        .toList();

        if (normalized.size() > MAX_DIFFICULTY_COUNT) {
            throw new CustomException(
                    ErrorCode.LIBRARY_DIFFICULTY_LIMIT_EXCEEDED
            );
        }

        boolean hasInvalidValue =
                normalized.stream()
                        .anyMatch(value ->
                                !DIFFICULTY_CODES.contains(value)
                        );

        if (hasInvalidValue) {
            throw new CustomException(
                    ErrorCode.LIBRARY_INVALID_FILTER
            );
        }

        return normalized;
    }

    // 요청된 모든 카테고리가 DB에 존재하는지 검증
    private void validateExistingCategories(
            List<Long> categoryIds
    ) {
        if (categoryIds.isEmpty()) {
            return;
        }

        long existingCount =
                libraryQueryRepository
                        .countExistingCategories(categoryIds);

        if (existingCount != categoryIds.size()) {
            throw new CustomException(
                    ErrorCode.LIBRARY_INVALID_FILTER
            );
        }
    }

    // 흐름별 카테고리 목록을 묶어 목록 응답 형태로 변환
    private Map<Long, List<LibraryListResponse.CategoryResponse>>
    groupListCategories(List<CategoryRow> rows) {
        return rows.stream()
                .collect(
                        Collectors.groupingBy(
                                CategoryRow::flowId,
                                LinkedHashMap::new,
                                Collectors.mapping(
                                        this::toListCategoryResponse,
                                        Collectors.toList()
                                )
                        )
                );
    }

    // 목록 응답의 카테고리 코드를 한글 표시명과 함께 변환
    private LibraryListResponse.CategoryResponse
    toListCategoryResponse(CategoryRow row) {
        CategoryCode categoryCode =
                parseCategoryCode(row);

        return new LibraryListResponse.CategoryResponse(
                row.categoryId(),
                categoryCode.name(),
                categoryCode.getDisplayName()
        );
    }

    // 상세 응답의 카테고리 코드를 한글 표시명과 함께 변환
    private LibraryDetailResponse.CategoryResponse
    toDetailCategoryResponse(CategoryRow row) {
        CategoryCode categoryCode =
                parseCategoryCode(row);

        return new LibraryDetailResponse.CategoryResponse(
                row.categoryId(),
                categoryCode.name(),
                categoryCode.getDisplayName()
        );
    }

    // DB의 categories.name 값을 CategoryCode로 변환
    private CategoryCode parseCategoryCode(CategoryRow row) {
        try {
            return CategoryCode.valueOf(
                    row.categoryCode()
            );
        } catch (
                IllegalArgumentException
                | NullPointerException exception
        ) {
            log.error(
                    "유효하지 않은 라이브러리 카테고리 코드: categoryId={}, code={}",
                    row.categoryId(),
                    row.categoryCode()
            );

            throw new CustomException(
                    ErrorCode.INTERNAL_SERVER_ERROR
            );
        }
    }

    // 인증 사용자는 email로 userId를 조회하고 비회원은 null을 반환
    private Long currentUserIdOrNull() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication
                instanceof AnonymousAuthenticationToken) {
            return null;
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .map(User::getUserId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );
    }
}