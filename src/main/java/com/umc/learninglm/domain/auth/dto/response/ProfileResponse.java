package com.umc.learninglm.domain.auth.dto.response;

import com.umc.learninglm.domain.auth.enums.UserProvider;
import com.umc.learninglm.domain.auth.enums.UserRole;
import com.umc.learninglm.domain.auth.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "프로필 수정 응답")
public record ProfileResponse(
		@Schema(example = "1") Long userId,
		@Schema(example = "new-user@example.com") String email,
		@Schema(example = "새닉네임") String nickname,
		@Schema(example = "LOCAL") UserProvider provider,
		@Schema(example = "USER") UserRole role,
		@Schema(example = "ACTIVE") UserStatus status,
		@Schema(type = "string", format = "date-time", example = "2026-07-14T21:00:00") LocalDateTime updatedAt
) {
}
