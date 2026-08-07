package com.umc.learninglm.domain.flow.repository;

import com.umc.learninglm.domain.flow.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {
}
