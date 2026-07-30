package com.umc.learninglm.domain.tutorial.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.learninglm.domain.auth.entity.User;
import com.umc.learninglm.domain.auth.repository.UserRepository;
import com.umc.learninglm.domain.block.entity.Block;
import com.umc.learninglm.domain.category.entity.Category;
import com.umc.learninglm.domain.category.enums.CategoryCode;
import com.umc.learninglm.domain.flow.entity.Flow;
import com.umc.learninglm.domain.tutorial.dto.response.TutorialBlockResponse;
import com.umc.learninglm.domain.tutorial.dto.response.TutorialCategoryResponse;
import com.umc.learninglm.domain.tutorial.dto.response.TutorialDetailResponse;
import com.umc.learninglm.domain.tutorial.dto.response.TutorialExampleResponse;
import com.umc.learninglm.domain.tutorial.dto.response.TutorialListResponse;
import com.umc.learninglm.domain.tutorial.dto.response.TutorialProgressResponse;
import com.umc.learninglm.domain.tutorial.dto.response.TutorialStepBlockResponse;
import com.umc.learninglm.domain.tutorial.dto.response.TutorialStepResponse;
import com.umc.learninglm.domain.tutorial.dto.response.TutorialStepsResponse;
import com.umc.learninglm.domain.tutorial.dto.response.TutorialSummaryResponse;
import com.umc.learninglm.domain.tutorial.dto.response.TutorialUseCaseResponse;
import com.umc.learninglm.domain.tutorial.entity.Tutorial;
import com.umc.learninglm.domain.tutorial.entity.TutorialBlock;
import com.umc.learninglm.domain.tutorial.entity.TutorialCategory;
import com.umc.learninglm.domain.tutorial.entity.TutorialStep;
import com.umc.learninglm.domain.tutorial.enums.Difficulty;
import com.umc.learninglm.domain.tutorial.enums.TutorialStatus;
import com.umc.learninglm.domain.tutorial.repository.SavedTutorialRepository;
import com.umc.learninglm.domain.tutorial.repository.TutorialBlockRepository;
import com.umc.learninglm.domain.tutorial.repository.TutorialCategoryRepository;
import com.umc.learninglm.domain.tutorial.repository.TutorialRepository;
import com.umc.learninglm.domain.tutorial.repository.TutorialStepRepository;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TutorialService {

	private final TutorialRepository tutorialRepository;
	private final TutorialCategoryRepository tutorialCategoryRepository;
	private final TutorialStepRepository tutorialStepRepository;
	private final TutorialBlockRepository tutorialBlockRepository;
	private final SavedTutorialRepository savedTutorialRepository;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;

	// 목록: status=PUBLISHED만, q(title·summary 부분일치)·categoryId·difficulty 필터, createdAt DESC.
	// 전체 반환(페이징 없음)이라 우선 in-memory 필터링. 데이터 증가 시 쿼리(Specification 등)로 최적화.
	@Transactional(readOnly = true)
	public TutorialListResponse getTutorials(String q, Long categoryId, String difficulty) {
		Difficulty difficultyFilter = parseDifficulty(difficulty);

		List<TutorialSummaryResponse> tutorials = tutorialRepository.findByStatus(TutorialStatus.PUBLISHED).stream()
				.filter(tutorial -> difficultyFilter == null || tutorial.getDifficulty() == difficultyFilter)
				.filter(tutorial -> matchesKeyword(tutorial, q))
				.filter(tutorial -> categoryId == null || hasCategory(tutorial.getTutorialId(), categoryId))
				.sorted(Comparator.comparing(Tutorial::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
				.map(this::toSummary)
				.toList();

		return new TutorialListResponse(tutorials.size(), tutorials);
	}

	// 상세: 요약·블록 흐름·활용 사례·예시 등. status=PUBLISHED 아니면 404.
	@Transactional(readOnly = true)
	public TutorialDetailResponse getTutorialDetail(Long tutorialId) {
		Tutorial tutorial = findPublishedOrThrow(tutorialId);

		List<TutorialBlockResponse> blockResponses = orderedTutorialBlocks(tutorialId).stream()
				.map(tutorialBlock -> {
					Block block = tutorialBlock.getBlock();
					return new TutorialBlockResponse(
							block.getBlockId(),
							block.getName(),
							block.getBlockType().name(),
							block.getDescription(),
							tutorialBlock.getReason());
				})
				.toList();
		List<String> blockFlow = blockResponses.stream().map(TutorialBlockResponse::name).toList();

		String flowType = null;
		TutorialExampleResponse example = null;
		Flow presetFlow = tutorial.getPresetFlow();
		if (presetFlow != null) {
			flowType = presetFlow.getFlowType().name();
			// source: 우선 TEMPLATE 고정. 정확한 출처 확인 필요.
			example = new TutorialExampleResponse(presetFlow.getExampleInput(), presetFlow.getExampleResult(), "TEMPLATE");
		}

		return new TutorialDetailResponse(
				tutorial.getTutorialId(),
				tutorial.getTitle(),
				tutorial.getSummary(),
				tutorial.getDifficulty().name(),
				loadCategories(tutorialId),
				blockResponses.size(),
				tutorial.getEstimatedMinutes(),
				parseUseCases(tutorial.getUseCases()),
				parseStringList(tutorial.getRequiredConcepts()),
				flowType,
				blockFlow,
				blockResponses,
				example,
				loadProgress(tutorialId)
		);
	}

	// 단계 + 단계별 추천 블록. 단계 데이터 없으면 404.
	@Transactional(readOnly = true)
	public TutorialStepsResponse getTutorialSteps(Long tutorialId) {
		Tutorial tutorial = findPublishedOrThrow(tutorialId);

		List<TutorialStep> steps = tutorialStepRepository.findByTutorial_TutorialIdOrderByStepOrderAsc(tutorialId);
		if (steps.isEmpty()) {
			throw new CustomException(ErrorCode.TUTORIAL_STEPS_NOT_FOUND);
		}

		List<TutorialStepResponse> stepResponses = steps.stream()
				.map(this::toStepResponse)
				.toList();

		return new TutorialStepsResponse(
				tutorial.getTutorialId(), tutorial.getTitle(), steps.size(), loadProgress(tutorialId), stepResponses);
	}

	// 선택적 인증 — 비로그인이면 null. 두 조회 API 모두 permitAll이라 토큰이 없어도 조회는 성공해야 한다.
	private TutorialProgressResponse loadProgress(Long tutorialId) {
		return currentUserOrEmpty()
				.flatMap(user -> savedTutorialRepository
						.findByUser_UserIdAndTutorial_TutorialId(user.getUserId(), tutorialId))
				.map(saved -> new TutorialProgressResponse(
						saved.getCurrentStepOrder(),
						saved.getStatus().name(),
						saved.getFlow() == null ? null : saved.getFlow().getFlowId()))
				.orElse(null);
	}

	// 토큰이 없거나 익명이면 빈 값. 잘못된 토큰은 JwtAuthenticationFilter가 먼저 걸러낸다(AUTH40104).
	private Optional<User> currentUserOrEmpty() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken) {
			return Optional.empty();
		}
		return userRepository.findByEmail(authentication.getName());
	}

	private Tutorial findPublishedOrThrow(Long tutorialId) {
		return tutorialRepository.findById(tutorialId)
				.filter(tutorial -> tutorial.getStatus() == TutorialStatus.PUBLISHED)
				.orElseThrow(() -> new CustomException(ErrorCode.TUTORIAL_NOT_FOUND));
	}

	private TutorialStepResponse toStepResponse(TutorialStep step) {
		List<TutorialStepBlockResponse> blockResponses =
				tutorialBlockRepository.findByTutorialStep_TutorialStepIdOrderByBlockOrderAsc(step.getTutorialStepId()).stream()
				.map(tutorialBlock -> {
					Block block = tutorialBlock.getBlock();
					return new TutorialStepBlockResponse(
							tutorialBlock.getTutorialBlockId(),
							block.getBlockId(),
							block.getName(),
							block.getBlockType().name(),
							tutorialBlock.getBlockOrder(),
							tutorialBlock.getRequired(),
							parseMap(tutorialBlock.getDefaultOptions()));
				})
				.toList();
		return new TutorialStepResponse(step.getTutorialStepId(), step.getStepOrder(), step.getTitle(), step.getDescription(), blockResponses);
	}

	private Difficulty parseDifficulty(String difficulty) {
		if (difficulty == null || difficulty.isBlank()) {
			return null;
		}
		try {
			return Difficulty.valueOf(difficulty.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new CustomException(ErrorCode.TUTORIAL_INVALID_FILTER);
		}
	}

	private boolean matchesKeyword(Tutorial tutorial, String q) {
		if (q == null || q.isBlank()) {
			return true;
		}
		String keyword = q.trim().toLowerCase();
		String title = tutorial.getTitle() == null ? "" : tutorial.getTitle().toLowerCase();
		String summary = tutorial.getSummary() == null ? "" : tutorial.getSummary().toLowerCase();
		return title.contains(keyword) || summary.contains(keyword);
	}

	private boolean hasCategory(Long tutorialId, Long categoryId) {
		return tutorialCategoryRepository.findByTutorial_TutorialId(tutorialId).stream()
				.anyMatch(tutorialCategory -> tutorialCategory.getCategory().getCategoryId().equals(categoryId));
	}

	private TutorialSummaryResponse toSummary(Tutorial tutorial) {
		return new TutorialSummaryResponse(
				tutorial.getTutorialId(),
				tutorial.getTitle(),
				tutorial.getSummary(),
				tutorial.getDifficulty().name(),
				loadCategories(tutorial.getTutorialId()),
				countBlocks(tutorial.getTutorialId()),
				tutorial.getEstimatedMinutes(),
				tutorial.getThumbnailUrl(),
				tutorial.getCreatedAt()
		);
	}

	private List<TutorialCategoryResponse> loadCategories(Long tutorialId) {
		return tutorialCategoryRepository.findByTutorial_TutorialId(tutorialId).stream()
				.map(TutorialCategory::getCategory)
				.map(this::toCategoryResponse)
				.toList();
	}

	private TutorialCategoryResponse toCategoryResponse(Category category) {
		String code = category.getName(); // categories.name = 코드값
		String displayName;
		try {
			displayName = CategoryCode.valueOf(code).getDisplayName();
		} catch (IllegalArgumentException e) {
			displayName = code; // 미정의 코드는 코드 그대로 노출
		}
		return new TutorialCategoryResponse(category.getCategoryId(), code, displayName);
	}

	// blockCount = 튜토리얼의 단계들에 속한 tutorial_blocks 개수 집계
	private int countBlocks(Long tutorialId) {
		List<Long> stepIds = tutorialStepRepository.findByTutorial_TutorialIdOrderByStepOrderAsc(tutorialId).stream()
				.map(TutorialStep::getTutorialStepId)
				.toList();
		if (stepIds.isEmpty()) {
			return 0;
		}
		return (int) tutorialBlockRepository.countByTutorialStep_TutorialStepIdIn(stepIds);
	}

	// 튜토리얼의 tutorial_blocks를 단계순서 → 블록순서로 정렬해 반환
	private List<TutorialBlock> orderedTutorialBlocks(Long tutorialId) {
		List<TutorialBlock> result = new ArrayList<>();
		for (TutorialStep step : tutorialStepRepository.findByTutorial_TutorialIdOrderByStepOrderAsc(tutorialId)) {
			result.addAll(tutorialBlockRepository.findByTutorialStep_TutorialStepIdOrderByBlockOrderAsc(step.getTutorialStepId()));
		}
		return result;
	}

	private List<TutorialUseCaseResponse> parseUseCases(String json) {
		if (json == null || json.isBlank()) {
			return List.of();
		}
		try {
			return objectMapper.readValue(json, new TypeReference<List<TutorialUseCaseResponse>>() {});
		} catch (JsonProcessingException e) {
			return List.of();
		}
	}

	private List<String> parseStringList(String json) {
		if (json == null || json.isBlank()) {
			return List.of();
		}
		try {
			return objectMapper.readValue(json, new TypeReference<List<String>>() {});
		} catch (JsonProcessingException e) {
			return List.of();
		}
	}

	private Map<String, Object> parseMap(String json) {
		if (json == null || json.isBlank()) {
			return null;
		}
		try {
			return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
		} catch (JsonProcessingException e) {
			return null;
		}
	}
}
