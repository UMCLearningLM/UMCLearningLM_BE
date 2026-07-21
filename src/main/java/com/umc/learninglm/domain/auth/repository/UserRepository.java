package com.umc.learninglm.domain.auth.repository;

import com.umc.learninglm.domain.auth.entity.User;
import com.umc.learninglm.domain.auth.enums.UserProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	Optional<User> findByProviderAndProviderId(UserProvider provider, String providerId);

	boolean existsByEmail(String email);
}
