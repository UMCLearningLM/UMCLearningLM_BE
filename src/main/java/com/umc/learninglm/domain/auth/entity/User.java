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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@Table(name = "users")
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
}
