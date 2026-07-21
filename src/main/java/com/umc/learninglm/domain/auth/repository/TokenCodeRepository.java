package com.umc.learninglm.domain.auth.repository;

import com.umc.learninglm.domain.auth.entity.TokenCode;
import com.umc.learninglm.domain.auth.enums.TokenStatus;
import com.umc.learninglm.domain.auth.enums.TokenType;
import com.umc.learninglm.domain.auth.enums.VerificationPurpose;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TokenCodeRepository extends JpaRepository<TokenCode, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<TokenCode> findFirstByTokenHashOrderByCreatedAtDesc(String tokenHash);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<TokenCode> findFirstByEmailAndTypeAndPurposeAndStatusOrderByCreatedAtDesc(
			String email,
			TokenType type,
			VerificationPurpose purpose,
			TokenStatus status);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<TokenCode> findFirstByTokenHashAndTypeOrderByCreatedAtDesc(String tokenHash, TokenType type);

	boolean existsByTokenHashAndTypeAndStatus(String tokenHash, TokenType type, TokenStatus status);

	List<TokenCode> findAllByEmailAndTypeAndPurposeAndStatus(
			String email,
			TokenType type,
			VerificationPurpose purpose,
			TokenStatus status);

	long countByEmailAndTypeAndCreatedAtAfter(String email, TokenType type, LocalDateTime createdAt);
}
