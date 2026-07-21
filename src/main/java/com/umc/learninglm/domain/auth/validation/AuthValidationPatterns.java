package com.umc.learninglm.domain.auth.validation;

public final class AuthValidationPatterns {

	public static final String PASSWORD =
			"^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z\\d\\s])\\S{8,20}$";
	public static final String NICKNAME = "^[가-힣A-Za-z0-9]{2,50}$";

	private AuthValidationPatterns() {
	}
}
