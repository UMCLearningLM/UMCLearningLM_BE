package com.umc.learninglm.domain.flow.repository;

import com.umc.learninglm.domain.flow.entity.Flow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlowRepository extends JpaRepository<Flow, Long> {
}
