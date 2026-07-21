package com.umc.learninglm.domain.auth.entity;

import com.umc.learninglm.domain.auth.enums.TokenStatus;
import com.umc.learninglm.domain.auth.enums.TokenType;
import com.umc.learninglm.domain.auth.enums.VerificationPurpose;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "token_code")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TokenCode {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "token_code_id", nullable = false)
	private Long tokenCodeId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "email", length = 255)
	private String email;

	@Column(name = "token_hash", nullable = false, length = 255)
	private String tokenHash;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 30)
	private TokenType type;

	@Enumerated(EnumType.STRING)
	@Column(name = "purpose", length = 30)
	private VerificationPurpose purpose;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	private TokenStatus status;

	@Column(name = "exp_time", nullable = false)
	private LocalDateTime expiresAt;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "used_at")
	private LocalDateTime usedAt;

	@Column(name = "revoked_at")
	private LocalDateTime revokedAt;

	@Column(name = "attempt_count", nullable = false)
	private int attemptCount;

	public static TokenCode createRefresh(User user, String tokenHash, LocalDateTime expiresAt) {
		return create(user, user.getEmail(), tokenHash, TokenType.REFRESH, expiresAt);
	}

	public static TokenCode createAccessBlacklist(User user, String tokenHash, LocalDateTime expiresAt) {
		return create(user, user.getEmail(), tokenHash, TokenType.ACCESS_BLACKLIST, expiresAt);
	}

	public static TokenCode createEmailCode(
			User user,
			String email,
			String tokenHash,
			VerificationPurpose purpose,
			LocalDateTime expiresAt) {
		return create(user, email, tokenHash, TokenType.EMAIL_CODE, purpose, expiresAt);
	}

	public static TokenCode createEmailVerification(
			User user,
			String email,
			String tokenHash,
			VerificationPurpose purpose,
			LocalDateTime expiresAt) {
		return create(user, email, tokenHash, TokenType.EMAIL_VERIFICATION, purpose, expiresAt);
	}

	private static TokenCode create(
			User user,
			String email,
			String tokenHash,
			TokenType type,
			LocalDateTime expiresAt) {
		return create(user, email, tokenHash, type, null, expiresAt);
	}

	private static TokenCode create(
			User user,
			String email,
			String tokenHash,
			TokenType type,
			VerificationPurpose purpose,
			LocalDateTime expiresAt) {
		TokenCode tokenCode = new TokenCode();
		tokenCode.user = user;
		tokenCode.email = email;
		tokenCode.tokenHash = tokenHash;
		tokenCode.type = type;
		tokenCode.purpose = purpose;
		tokenCode.status = TokenStatus.ACTIVE;
		tokenCode.expiresAt = expiresAt;
		tokenCode.attemptCount = 0;
		return tokenCode;
	}

	public boolean isExpired(LocalDateTime now) {
		return status == TokenStatus.EXPIRED || !expiresAt.isAfter(now);
	}

	public boolean isActive(LocalDateTime now) {
		return status == TokenStatus.ACTIVE && !isExpired(now);
	}

	public void markUsed(LocalDateTime usedAt) {
		this.status = TokenStatus.USED;
		this.usedAt = usedAt;
	}

	public void revoke(LocalDateTime revokedAt) {
		this.status = TokenStatus.REVOKED;
		this.revokedAt = revokedAt;
	}

	public void recordFailedAttempt() {
		this.attemptCount++;
	}
}
