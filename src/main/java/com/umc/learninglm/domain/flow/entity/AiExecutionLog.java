package com.umc.learninglm.domain.flow.entity;

import com.umc.learninglm.domain.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "ai_execution_logs")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiExecutionLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ai_execution_log_id", nullable = false)
	private Long aiExecutionLogId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "flow_id")
	private Flow flow;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "flow_block_id")
	private FlowBlock flowBlock;

	@Column(name = "provider", length = 50)
	private String provider;

	@Column(name = "model_name", length = 100)
	private String modelName;

	@Column(name = "request_type", length = 50)
	private String requestType;

	@Column(name = "status", nullable = false, length = 30)
	private String status;

	@Column(name = "error_message", columnDefinition = "TEXT")
	private String errorMessage;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "input_snapshot", columnDefinition = "JSON")
	private String inputSnapshot;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "output_snapshot", columnDefinition = "JSON")
	private String outputSnapshot;

	@Column(name = "token_usage")
	private Integer tokenUsage;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public static AiExecutionLog create(
			User user,
			Flow flow,
			String provider,
			String modelName,
			String requestType,
			String status,
			String errorMessage,
			String inputSnapshot,
			String outputSnapshot,
			Integer tokenUsage) {
		AiExecutionLog log = new AiExecutionLog();
		log.user = user;
		log.flow = flow;
		log.provider = provider;
		log.modelName = modelName;
		log.requestType = requestType;
		log.status = status;
		log.errorMessage = errorMessage;
		log.inputSnapshot = inputSnapshot;
		log.outputSnapshot = outputSnapshot;
		log.tokenUsage = tokenUsage;
		return log;
	}
}
