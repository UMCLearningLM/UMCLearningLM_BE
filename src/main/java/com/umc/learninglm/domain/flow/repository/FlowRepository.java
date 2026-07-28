package com.umc.learninglm.domain.flow.repository;

import com.umc.learninglm.domain.flow.entitiy.Flow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlowRepository extends JpaRepository<Flow, Long> {
}
