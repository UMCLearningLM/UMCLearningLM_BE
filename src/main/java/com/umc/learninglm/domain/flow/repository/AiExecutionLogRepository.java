package com.umc.learninglm.domain.flow.repository;

import com.umc.learninglm.domain.flow.entity.AiExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiExecutionLogRepository extends JpaRepository<AiExecutionLog, Long> {
}
