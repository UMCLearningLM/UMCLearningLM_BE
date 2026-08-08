package com.umc.learninglm.domain.block.repository;

import com.umc.learninglm.domain.block.entity.PromptTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {
}
