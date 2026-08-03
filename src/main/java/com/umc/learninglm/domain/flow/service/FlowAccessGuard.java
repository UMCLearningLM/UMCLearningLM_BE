package com.umc.learninglm.domain.flow.service;

import com.umc.learninglm.domain.auth.entity.User;
import com.umc.learninglm.domain.auth.repository.UserRepository;
import com.umc.learninglm.domain.flow.entity.Flow;
import com.umc.learninglm.domain.flow.repository.FlowRepository;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FlowAccessGuard {

	private final FlowRepository flowRepository;
	private final UserRepository userRepository;

	// JWT 필터가 principal에 email을 넣음(auth 도메인 컨벤션) → email로 사용자 조회.
	public User currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new CustomException(ErrorCode.ACCESS_TOKEN_MISSING);
		}
		String email = authentication.getName();
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
	}

	public Flow requireFlow(Long flowId) {
		return flowRepository.findById(flowId)
				.orElseThrow(() -> new CustomException(ErrorCode.FLOW_NOT_FOUND));
	}

	public void requireOwner(Flow flow, User user) {
		if (!flow.getUser().getUserId().equals(user.getUserId())) {
			throw new CustomException(ErrorCode.FLOW_OWNER_MISMATCH);
		}
	}
}
