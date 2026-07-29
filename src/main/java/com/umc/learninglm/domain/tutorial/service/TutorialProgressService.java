package com.umc.learninglm.domain.tutorial.service;

import com.umc.learninglm.domain.auth.entity.User;
import com.umc.learninglm.domain.auth.repository.UserRepository;
import com.umc.learninglm.domain.flow.entity.Flow;
import com.umc.learninglm.domain.flow.repository.FlowRepository;
import com.umc.learninglm.domain.tutorial.dto.response.TutorialProgressSaveResponse;
import com.umc.learninglm.domain.tutorial.dto.response.TutorialProgressStartResponse;
import com.umc.learninglm.domain.tutorial.dto.response.TutorialProgressUpdateResponse;
import com.umc.learninglm.domain.tutorial.entity.SavedTutorial;
import com.umc.learninglm.domain.tutorial.entity.Tutorial;
import com.umc.learninglm.domain.tutorial.enums.SavedTutorialStatus;
import com.umc.learninglm.domain.tutorial.enums.TutorialStatus;
import com.umc.learninglm.domain.tutorial.repository.SavedTutorialRepository;
import com.umc.learninglm.domain.tutorial.repository.TutorialRepository;
import com.umc.learninglm.domain.tutorial.repository.TutorialStepRepository;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TutorialProgressService {

	private static final String STATUS_COMPLETED = "COMPLETED";

	private final SavedTutorialRepository savedTutorialRepository;
	private final TutorialRepository tutorialRepository;
	private final TutorialStepRepository tutorialStepRepository;
	private final FlowRepository flowRepository;
	private final UserRepository userRepository;

	// 저장(북마크): saved_tutorials NOT_STARTED 생성
	@Transactional
	public TutorialProgressSaveResponse saveTutorial(Long tutorialId) {
		User user = currentUser();
		Tutorial tutorial = requirePublishedTutorial(tutorialId);
		if (savedTutorialRepository.findByUser_UserIdAndTutorial_TutorialId(user.getUserId(), tutorialId).isPresent()) {
			throw new CustomException(ErrorCode.TUTORIAL_ALREADY_SAVED);
		}
		SavedTutorial saved;
		try {
			saved = savedTutorialRepository.saveAndFlush(SavedTutorial.createBookmark(user, tutorial));
		} catch (DataIntegrityViolationException e) {
			// 위 조회 이후 동시 요청이 먼저 저장한 경우 — uq_saved_tutorial 위반
			throw new CustomException(ErrorCode.TUTORIAL_ALREADY_SAVED);
		}
		int totalSteps = totalSteps(tutorialId);
		return new TutorialProgressSaveResponse(
				tutorialId,
				saved.getCurrentStepOrder(),
				totalSteps,
				progressRate(saved.getStatus(), saved.getCurrentStepOrder(), totalSteps),
				saved.getStatus().name(),
				saved.getCreatedAt());
	}

	// 시작: IN_PROGRESS 전환 + flow 연결. 저장 이력 없으면 생성(upsert).
	// 진행도는 특정 flow에서 단계별 필수 블록을 채운 정도라, 같은 flow 재호출일 때만 진행 상태를 보존(멱등).
	// 진행 중인데 다른 flow로 호출하면 진행 기록이 소실되므로 거부(TUTORIAL40902). 저장 해제 후 재시작해야 함.
	@Transactional
	public TutorialProgressStartResponse startTutorial(Long tutorialId, Long flowId) {
		User user = currentUser();
		Tutorial tutorial = requirePublishedTutorial(tutorialId);

		Flow flow = flowRepository.findById(flowId).orElse(null);
		if (flow == null || flow.getTutorial() == null
				|| !tutorialId.equals(flow.getTutorial().getTutorialId())) {
			throw new CustomException(ErrorCode.TUTORIAL_FLOW_MISMATCH);
		}

		SavedTutorial saved = savedTutorialRepository.findByUser_UserIdAndTutorial_TutorialId(user.getUserId(), tutorialId)
				.orElseGet(() -> SavedTutorial.createBookmark(user, tutorial));
		if (saved.getStatus() == SavedTutorialStatus.IN_PROGRESS) {
			Flow currentFlow = saved.getFlow();
			if (currentFlow == null || !flowId.equals(currentFlow.getFlowId())) {
				throw new CustomException(ErrorCode.TUTORIAL_ALREADY_STARTED);
			}
			// 같은 flow 재호출 — 진행 상태 그대로 유지(멱등)
		} else {
			saved.start(flow);
		}
		saved = savedTutorialRepository.saveAndFlush(saved);

		int totalSteps = totalSteps(tutorialId);
		return new TutorialProgressStartResponse(
				tutorialId,
				saved.getCurrentStepOrder(),
				totalSteps,
				progressRate(saved.getStatus(), saved.getCurrentStepOrder(), totalSteps),
				saved.getStatus().name(),
				saved.getFlow().getFlowId(),
				saved.getUpdatedAt());
	}

	// 갱신(이어하기): 현재 단계 값 갱신. status=COMPLETED 전송 시 완료 처리.
	@Transactional
	public TutorialProgressUpdateResponse updateProgress(Long tutorialId, Integer currentStepOrder, String status) {
		User user = currentUser();
		requirePublishedTutorial(tutorialId);

		SavedTutorial saved = savedTutorialRepository.findByUser_UserIdAndTutorial_TutorialId(user.getUserId(), tutorialId)
				.orElseThrow(() -> new CustomException(ErrorCode.TUTORIAL_PROGRESS_NOT_FOUND));
		if (saved.getStatus() == SavedTutorialStatus.NOT_STARTED) {
			throw new CustomException(ErrorCode.TUTORIAL_NOT_STARTED);
		}

		int totalSteps = totalSteps(tutorialId);
		if (currentStepOrder == null || currentStepOrder < 1 || currentStepOrder > totalSteps) {
			throw new CustomException(ErrorCode.TUTORIAL_INVALID_STEP);
		}
		if (status != null && !STATUS_COMPLETED.equals(status)) {
			throw new CustomException(ErrorCode.TUTORIAL_INVALID_STEP);
		}
		// 완료 처리는 마지막 단계에서만 허용
		if (STATUS_COMPLETED.equals(status) && currentStepOrder != totalSteps) {
			throw new CustomException(ErrorCode.TUTORIAL_INVALID_STEP);
		}

		if (STATUS_COMPLETED.equals(status)) {
			saved.complete(currentStepOrder);
		} else {
			saved.updateStep(currentStepOrder);
		}
		saved = savedTutorialRepository.saveAndFlush(saved);

		return new TutorialProgressUpdateResponse(
				tutorialId,
				saved.getCurrentStepOrder(),
				totalSteps,
				progressRate(saved.getStatus(), saved.getCurrentStepOrder(), totalSteps),
				saved.getStatus().name(),
				saved.getUpdatedAt());
	}

	// 저장 해제(하드 삭제)
	@Transactional
	public void deleteProgress(Long tutorialId) {
		User user = currentUser();
		requirePublishedTutorial(tutorialId);
		SavedTutorial saved = savedTutorialRepository.findByUser_UserIdAndTutorial_TutorialId(user.getUserId(), tutorialId)
				.orElseThrow(() -> new CustomException(ErrorCode.TUTORIAL_PROGRESS_NOT_FOUND));
		savedTutorialRepository.delete(saved);
	}

	private Tutorial requirePublishedTutorial(Long tutorialId) {
		Tutorial tutorial = tutorialRepository.findById(tutorialId)
				.orElseThrow(() -> new CustomException(ErrorCode.TUTORIAL_NOT_FOUND));
		if (tutorial.getStatus() != TutorialStatus.PUBLISHED) {
			throw new CustomException(ErrorCode.TUTORIAL_NOT_FOUND);
		}
		return tutorial;
	}

	private int totalSteps(Long tutorialId) {
		return (int) tutorialStepRepository.countByTutorial_TutorialId(tutorialId);
	}

	// progressRate: COMPLETED면 100, 그 외 round((currentStepOrder - 1) / totalSteps * 100)
	private int progressRate(SavedTutorialStatus status, int currentStepOrder, int totalSteps) {
		if (status == SavedTutorialStatus.COMPLETED) {
			return 100;
		}
		if (totalSteps <= 0) {
			return 0;
		}
		return (int) Math.round((currentStepOrder - 1) * 100.0 / totalSteps);
	}

	// JWT 필터가 principal에 email을 넣음(auth 도메인 컨벤션) → email로 사용자 조회.
	private User currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			// 이 엔드포인트들은 SecurityConfig에서 authenticated()로 보호됨. 방어적 처리.
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
		String email = authentication.getName();
		return userRepository.findByEmail(email)
				// TODO: auth 병합 후 AUTH40401(사용자 없음)로 교체
				.orElseThrow(() -> new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));
	}
}
