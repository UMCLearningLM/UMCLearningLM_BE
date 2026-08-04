package com.umc.learninglm.domain.storage.service;

import com.umc.learninglm.domain.auth.entity.User;
import com.umc.learninglm.domain.auth.repository.UserRepository;
import com.umc.learninglm.domain.category.enums.CategoryCode;
import com.umc.learninglm.domain.flow.enums.FlowMode;
import com.umc.learninglm.domain.flow.repository.FlowCategoryRepository;
import com.umc.learninglm.domain.flow.repository.FlowRepository;
import com.umc.learninglm.domain.storage.dto.query.MyFlowQuery;
import com.umc.learninglm.domain.storage.dto.query.SavedTutorialQuery;
import com.umc.learninglm.domain.storage.dto.query.StorageCategoryQuery;
import com.umc.learninglm.domain.storage.dto.response.StorageCategoryResponse;
import com.umc.learninglm.domain.storage.dto.response.StorageCountsResponse;
import com.umc.learninglm.domain.storage.dto.response.StorageFlowListResponse;
import com.umc.learninglm.domain.storage.dto.response.StorageFlowResponse;
import com.umc.learninglm.domain.storage.dto.response.StorageTutorialListResponse;
import com.umc.learninglm.domain.storage.dto.response.StorageTutorialResponse;
import com.umc.learninglm.domain.tutorial.enums.SavedTutorialStatus;
import com.umc.learninglm.domain.tutorial.repository.SavedTutorialRepository;
import com.umc.learninglm.domain.tutorial.service.TutorialProgressCalculator;
import com.umc.learninglm.domain.tutorial.repository.TutorialCategoryRepository;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
public class StorageService {

    private static final String TYPE_OWN = "OWN";
    private static final String TYPE_COPIED = "COPIED";

    private final SavedTutorialRepository savedTutorialRepository;
    private final TutorialCategoryRepository tutorialCategoryRepository;
    private final FlowRepository flowRepository;
    private final FlowCategoryRepository flowCategoryRepository;
    private final UserRepository userRepository;

    // 저장한 튜토리얼 목록. 진행률은 튜토리얼 도메인과 동일한 기준으로 계산한다.
    public StorageTutorialListResponse getSavedTutorials() {
        Long userId = currentUserId();

        List<SavedTutorialQuery> rows =
                savedTutorialRepository.findSavedTutorials(userId);

        StorageCountsResponse counts = loadCounts(userId);

        if (rows.isEmpty()) {
            return new StorageTutorialListResponse(0, List.of(), counts);
        }

        List<Long> tutorialIds = rows.stream()
                .map(SavedTutorialQuery::tutorialId)
                .toList();

        Map<Long, List<StorageCategoryResponse>> categoryMap =
                groupCategories(
                        tutorialCategoryRepository
                                .findStorageCategoriesByTutorialIds(tutorialIds)
                );

        List<StorageTutorialResponse> tutorials = rows.stream()
                .map(row -> new StorageTutorialResponse(
                        row.tutorialId(),
                        row.title(),
                        row.summary(),
                        row.difficulty(),
                        categoryMap.getOrDefault(row.tutorialId(), List.of()),
                        row.thumbnailUrl(),
                        row.status(),
                        row.currentStepOrder(),
                        row.totalSteps().intValue(),
                        TutorialProgressCalculator.progressRate(
                                SavedTutorialStatus.valueOf(row.status()),
                                row.currentStepOrder(),
                                row.totalSteps().intValue()
                        ),
                        row.flowId(),
                        row.createdAt(),
                        row.updatedAt()
                ))
                .toList();

        return new StorageTutorialListResponse(
                tutorials.size(),
                tutorials,
                counts
        );
    }

    // 내가 만든(own) 또는 복사한(copied) 흐름 목록
    public StorageFlowListResponse getMyFlows(String type) {
        boolean copiedOnly = parseCopiedOnly(type);

        Long userId = currentUserId();

        List<MyFlowQuery> rows = copiedOnly
                ? flowRepository.findMyCopiedFlows(userId)
                : flowRepository.findMyOwnFlows(userId);

        StorageCountsResponse counts = loadCounts(userId);

        if (rows.isEmpty()) {
            return new StorageFlowListResponse(0, List.of(), counts);
        }

        List<Long> flowIds = rows.stream()
                .map(MyFlowQuery::flowId)
                .toList();

        Map<Long, List<StorageCategoryResponse>> categoryMap =
                groupCategories(
                        flowCategoryRepository
                                .findStorageCategoriesByFlowIds(flowIds)
                );

        List<StorageFlowResponse> flows = rows.stream()
                .map(row -> new StorageFlowResponse(
                        row.flowId(),
                        row.title(),
                        row.summary(),
                        row.difficulty(),
                        categoryMap.getOrDefault(row.flowId(), List.of()),
                        row.mode(),
                        row.visibility(),
                        row.status(),
                        row.originFlowId(),
                        row.originalAuthorNickname(),
                        row.updatedAt()
                ))
                .toList();

        return new StorageFlowListResponse(
                flows.size(),
                flows,
                counts
        );
    }

    // type=own → false(원본만), type=copied → true(복사본만). 그 외 값은 거부.
    private boolean parseCopiedOnly(String type) {
        if (type == null || type.isBlank()) {
            throw new CustomException(ErrorCode.STORAGE_INVALID_PARAMETER);
        }

        String normalized = type.trim().toUpperCase(Locale.ROOT);

        if (TYPE_OWN.equals(normalized)) {
            return false;
        }
        if (TYPE_COPIED.equals(normalized)) {
            return true;
        }

        throw new CustomException(ErrorCode.STORAGE_INVALID_PARAMETER);
    }

    private StorageCountsResponse loadCounts(Long userId) {
        return new StorageCountsResponse(
                (int) savedTutorialRepository.countPublishedByUserId(userId),
                (int) flowRepository
                        .countByUser_UserIdAndModeAndOriginFlowIsNull(
                                userId, FlowMode.CREATE
                        ),
                (int) flowRepository
                        .countByUser_UserIdAndModeAndOriginFlowIsNotNull(
                                userId, FlowMode.CREATE
                        )
        );
    }

    // 소유자(튜토리얼 또는 흐름)별 카테고리 목록으로 묶는다
    private Map<Long, List<StorageCategoryResponse>> groupCategories(
            List<StorageCategoryQuery> rows
    ) {
        return rows.stream()
                .collect(Collectors.groupingBy(
                        StorageCategoryQuery::ownerId,
                        LinkedHashMap::new,
                        Collectors.mapping(
                                this::toCategoryResponse,
                                Collectors.toList()
                        )
                ));
    }

    // categories.name에 저장된 코드값을 CategoryCode로 변환해 표시명을 채운다
    private StorageCategoryResponse toCategoryResponse(
            StorageCategoryQuery row
    ) {
        CategoryCode categoryCode =
                parseCategoryCode(row.categoryId(), row.code());

        return new StorageCategoryResponse(
                row.categoryId(),
                categoryCode.name(),
                categoryCode.getDisplayName()
        );
    }

    private CategoryCode parseCategoryCode(Long categoryId, String code) {
        try {
            return CategoryCode.valueOf(code);
        } catch (IllegalArgumentException | NullPointerException exception) {
            log.error(
                    "유효하지 않은 저장소 카테고리 코드: categoryId={}, code={}",
                    categoryId,
                    code
            );

            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // JWT 필터가 principal에 email을 넣으므로 email로 사용자를 조회한다.
    // 내 저장소는 인증이 필수라 인증 정보가 없으면 예외로 처리한다.
    private Long currentUserId() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        // AnonymousAuthenticationToken은 isAuthenticated()가 true라 따로 걸러낸다
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new CustomException(ErrorCode.ACCESS_TOKEN_MISSING);
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .map(User::getUserId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );
    }
}
