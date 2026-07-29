package com.umc.learninglm.domain.auth.service;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class TokenHashService {

	private static final String HASH_ALGORITHM = "SHA-256";

	public String hash(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
			byte[] hashedValue = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashedValue);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
		}
	}
}
