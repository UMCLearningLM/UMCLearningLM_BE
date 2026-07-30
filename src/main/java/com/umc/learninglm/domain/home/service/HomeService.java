package com.umc.learninglm.domain.home.service;

import com.umc.learninglm.domain.category.enums.CategoryCode;
import com.umc.learninglm.domain.category.repository.CategoryRepository;
import com.umc.learninglm.domain.flow.repository.FlowCategoryRepository;
import com.umc.learninglm.domain.flow.repository.FlowRepository;
import com.umc.learninglm.domain.home.dto.query.FlowCategoryQuery;
import com.umc.learninglm.domain.home.dto.query.*;
import com.umc.learninglm.domain.home.dto.query.TutorialCategoryQuery;
import com.umc.learninglm.domain.home.dto.response.HomeResponse;
import com.umc.learninglm.domain.tutorial.repository.SavedTutorialRepository;
import com.umc.learninglm.domain.tutorial.repository.TutorialCategoryRepository;
import com.umc.learninglm.domain.tutorial.repository.TutorialRepository;
import com.umc.learninglm.domain.auth.entity.User;
import com.umc.learninglm.domain.auth.repository.UserRepository;
import com.umc.learninglm.global.error.ErrorCode;
import com.umc.learninglm.global.error.CustomException;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    // 홈 화면 카테고리를 와이어프레임에서 정의한 순서로 반환
    private static final List<CategoryCode> HOME_CATEGORY_ORDER = List.of(
            CategoryCode.RESEARCH,
            CategoryCode.DOCUMENT_SUMMARY,
            CategoryCode.WRITING,
            CategoryCode.ROUTINE,
            CategoryCode.REVIEW,
            CategoryCode.AI_TOOL
    );

    private final UserRepository userRepository;
    private final SavedTutorialRepository savedTutorialRepository;
    private final TutorialRepository tutorialRepository;
    private final TutorialCategoryRepository tutorialCategoryRepository;
    private final CategoryRepository categoryRepository;
    private final FlowRepository flowRepository;
    private final FlowCategoryRepository flowCategoryRepository;

    // 인증 여부에 따라 공통 홈 데이터와 회원 개인화 데이터를 조합
    public HomeResponse getHome() {
        User user = currentUserOrNull();
        boolean isGuest = user == null;
        Long userId = isGuest ? null : user.getUserId();

        HomeResponse.ContinueLearningResponse continueLearning =
                isGuest ? null : getContinueLearning(userId);

        List<HomeResponse.RecommendedTutorialResponse> recommendedTutorials =
                getRecommendedTutorials(userId);

        List<HomeResponse.CategoryResponse> categories =
                getHomeCategories();

        List<HomeResponse.PopularFlowResponse> popularFlows =
                getPopularFlows();

        List<HomeResponse.RecentSavedItemResponse> recentSavedItems =
                isGuest ? List.of() : getRecentSavedItems(userId);

        return new HomeResponse(
                isGuest,
                continueLearning,
                recommendedTutorials,
                categories,
                popularFlows,
                recentSavedItems
        );
    }

    // JWT 필터가 principal에 email을 넣으므로 인증된 요청은 email로 사용자를 조회
    private User currentUserOrNull() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.USER_NOT_FOUND)
                );
    }

    // 사용자가 가장 최근에 진행한 IN_PROGRESS 튜토리얼 한 건을 조회
    private HomeResponse.ContinueLearningResponse getContinueLearning(
            Long userId
    ) {
        Pageable limitOne = PageRequest.of(0, 1);

        return savedTutorialRepository
                .findContinueLearning(userId, limitOne)
                .stream()
                .findFirst()
                .map(query -> {
                    int totalSteps = query.totalSteps().intValue();

                    // 현재 학습 중인 단계의 이전 단계까지 완료한 것으로 계산
                    int completedStepCount = Math.max(
                            query.currentStepOrder() - 1,
                            0
                    );

                    int progressRate = totalSteps == 0
                            ? 0
                            : completedStepCount * 100 / totalSteps;

                    return new HomeResponse.ContinueLearningResponse(
                            query.tutorialId(),
                            query.flowId(),
                            query.title(),
                            query.difficulty(),
                            query.currentStepOrder(),
                            totalSteps,
                            completedStepCount,
                            progressRate,
                            query.status(),
                            query.thumbnailUrl(),
                            query.updatedAt()
                    );
                })
                .orElse(null);
    }

    // 게스트는 최신 튜토리얼을, 회원은 진행 중인 튜토리얼을 제외한 최신 튜토리얼을 조회
    private List<HomeResponse.RecommendedTutorialResponse>
    getRecommendedTutorials(Long userId) {

        Pageable limitThree = PageRequest.of(0, 3);

        List<RecommendedTutorialQuery> tutorials =
                userId == null
                        ? tutorialRepository
                        .findRecommendedTutorialsForGuest(limitThree)
                        : tutorialRepository
                        .findRecommendedTutorialsForUser(
                                userId,
                                limitThree
                        );

        if (tutorials.isEmpty()) {
            return List.of();
        }

        List<Long> tutorialIds = tutorials.stream()
                .map(RecommendedTutorialQuery::tutorialId)
                .toList();

        // 추천 튜토리얼의 카테고리를 한 번에 조회해 튜토리얼별로 묶음
        Map<Long, List<HomeResponse.CategoryResponse>> categoryMap =
                tutorialCategoryRepository.findByTutorialIds(tutorialIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                TutorialCategoryQuery::tutorialId,
                                LinkedHashMap::new,
                                Collectors.mapping(
                                        this::toTutorialCategoryResponse,
                                        Collectors.toList()
                                )
                        ));

        return tutorials.stream()
                .map(tutorial ->
                        new HomeResponse.RecommendedTutorialResponse(
                                tutorial.tutorialId(),
                                tutorial.title(),
                                tutorial.summary(),
                                tutorial.difficulty(),
                                categoryMap.getOrDefault(
                                        tutorial.tutorialId(),
                                        List.of()
                                ),
                                tutorial.blockCount().intValue(),
                                tutorial.estimatedMinutes(),
                                tutorial.thumbnailUrl()
                        )
                )
                .toList();
    }

    // 홈 화면에 노출할 목적별 카테고리만 조회
    private List<HomeResponse.CategoryResponse> getHomeCategories() {
        List<String> categoryCodes = HOME_CATEGORY_ORDER.stream()
                .map(CategoryCode::name)
                .toList();

        List<CategoryQuery> queriedCategories =
                categoryRepository.findHomeCategories(categoryCodes);

        Map<CategoryCode, HomeResponse.CategoryResponse> categoryMap =
                new EnumMap<>(CategoryCode.class);

        for (CategoryQuery query : queriedCategories) {
            CategoryCode categoryCode =
                    CategoryCode.valueOf(query.code());

            categoryMap.put(
                    categoryCode,
                    new HomeResponse.CategoryResponse(
                            query.categoryId(),
                            categoryCode.name(),
                            categoryCode.getDisplayName()
                    )
            );
        }

        // DB의 sortOrder가 아닌 홈 화면에서 정의한 순서로 카테고리를 반환
        return HOME_CATEGORY_ORDER.stream()
                .map(categoryMap::get)
                .toList();
    }

    // 좋아요 수를 기준으로 인기 공개 흐름 세 건을 조회
    private List<HomeResponse.PopularFlowResponse> getPopularFlows() {
        Pageable limitThree = PageRequest.of(0, 3);

        List<PopularFlowQuery> flows =
                flowRepository.findPopularFlows(limitThree);

        if (flows.isEmpty()) {
            return List.of();
        }

        List<Long> flowIds = flows.stream()
                .map(PopularFlowQuery::flowId)
                .toList();

        // 인기 흐름에 연결된 모든 카테고리를 흐름별로 묶음
        Map<Long, List<HomeResponse.CategoryResponse>> categoryMap =
                flowCategoryRepository.findByFlowIds(flowIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                FlowCategoryQuery::flowId,
                                LinkedHashMap::new,
                                Collectors.mapping(
                                        this::toFlowCategoryResponse,
                                        Collectors.toList()
                                )
                        ));

        return flows.stream()
                .map(flow ->
                        new HomeResponse.PopularFlowResponse(
                                flow.flowId(),
                                flow.title(),
                                flow.summary(),
                                flow.difficulty(),
                                categoryMap.getOrDefault(
                                        flow.flowId(),
                                        List.of()
                                ),
                                new HomeResponse.AuthorResponse(
                                        flow.authorId(),
                                        flow.authorNickname()
                                ),
                                flow.likeCount(),
                                flow.copyCount(),
                                flow.commentCount()
                        )
                )
                .toList();
    }

    // 최근 저장 튜토리얼과 최근 복사 흐름을 합쳐 최신 두 건을 반환
    private List<HomeResponse.RecentSavedItemResponse> getRecentSavedItems(
            Long userId
    ) {
        Pageable limitTwo = PageRequest.of(0, 2);

        Stream<HomeResponse.RecentSavedItemResponse> tutorialItems =
                savedTutorialRepository
                        .findRecentTutorials(userId, limitTwo)
                        .stream()
                        .map(query ->
                                new HomeResponse.TutorialSavedItemResponse(
                                        query.tutorialId(),
                                        query.flowId(),
                                        query.title(),
                                        query.difficulty(),
                                        query.status(),
                                        query.thumbnailUrl(),
                                        query.savedAt()
                                )
                        );

        Stream<HomeResponse.RecentSavedItemResponse> copiedFlowItems =
                flowRepository
                        .findRecentCopiedFlows(userId, limitTwo)
                        .stream()
                        .map(query ->
                                new HomeResponse.CopiedFlowSavedItemResponse(
                                        query.flowId(),
                                        query.originFlowId(),
                                        query.title(),
                                        query.difficulty(),
                                        new HomeResponse.AuthorResponse(
                                                query.originalAuthorId(),
                                                query.originalAuthorNickname()
                                        ),
                                        null,
                                        query.savedAt()
                                )
                        );

        return Stream.concat(tutorialItems, copiedFlowItems)
                .sorted(
                        Comparator.comparing(
                                HomeResponse.RecentSavedItemResponse::savedAt
                        ).reversed()
                )
                .limit(2)
                .toList();
    }

    // 튜토리얼 카테고리 조회 결과를 홈 응답 DTO로 변환
    private HomeResponse.CategoryResponse toTutorialCategoryResponse(
            TutorialCategoryQuery query
    ) {
        CategoryCode categoryCode =
                CategoryCode.valueOf(query.code());

        return new HomeResponse.CategoryResponse(
                query.categoryId(),
                categoryCode.name(),
                categoryCode.getDisplayName()
        );
    }

    // 흐름 카테고리 조회 결과를 홈 응답 DTO로 변환
    private HomeResponse.CategoryResponse toFlowCategoryResponse(
            FlowCategoryQuery query
    ) {
        CategoryCode categoryCode =
                CategoryCode.valueOf(query.code());

        return new HomeResponse.CategoryResponse(
                query.categoryId(),
                categoryCode.name(),
                categoryCode.getDisplayName()
        );
    }
}