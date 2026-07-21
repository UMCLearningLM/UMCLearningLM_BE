package com.umc.learninglm.domain.auth.entity;

import com.umc.learninglm.domain.auth.enums.UserProvider;
import com.umc.learninglm.domain.auth.enums.UserRole;
import com.umc.learninglm.domain.auth.enums.UserStatus;
import com.umc.learninglm.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@Table(name = "users", uniqueConstraints = {
		@UniqueConstraint(name = "uk_users_email", columnNames = "email"),
		@UniqueConstraint(name = "uk_users_provider_provider_id", columnNames = {"provider", "provider_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "email", nullable = false, length = 255)
	private String email;

	@Column(name = "password_hash", length = 255)
	private String passwordHash;

	@Column(name = "provider_id", length = 255)
	private String providerId;

	@Column(name = "nickname", nullable = false, length = 50)
	private String nickname;

	@Enumerated(EnumType.STRING)
	@Column(name = "provider", nullable = false, length = 30)
	@ColumnDefault("'LOCAL'")
	private UserProvider provider = UserProvider.LOCAL;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 30)
	@ColumnDefault("'USER'")
	private UserRole role = UserRole.USER;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	@ColumnDefault("'ACTIVE'")
	private UserStatus status = UserStatus.ACTIVE;

	public static User createLocal(String email, String passwordHash, String nickname) {
		User user = new User();
		user.email = email;
		user.passwordHash = passwordHash;
		user.providerId = null;
		user.nickname = nickname;
		user.provider = UserProvider.LOCAL;
		user.role = UserRole.USER;
		user.status = UserStatus.ACTIVE;
		return user;
	}

	public static User createSocial(
			String email,
			UserProvider provider,
			String providerId,
			String nickname) {
		User user = new User();
		user.email = email;
		user.passwordHash = null;
		user.providerId = providerId;
		user.nickname = nickname;
		user.provider = provider;
		user.role = UserRole.USER;
		user.status = UserStatus.ACTIVE;
		return user;
	}

	public void changePassword(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public void changeEmail(String email) {
		this.email = email;
	}

	public void changeNickname(String nickname) {
		this.nickname = nickname;
	}
}
