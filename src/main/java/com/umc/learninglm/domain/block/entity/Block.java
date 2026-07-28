package com.umc.learninglm.domain.block.entity;

import com.umc.learninglm.domain.block.enums.BlockStatus;
import com.umc.learninglm.domain.block.enums.BlockType;
import com.umc.learninglm.domain.block.enums.ExecutionMode;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "blocks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Block extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "block_id", nullable = false)
	private Long blockId;

	@Enumerated(EnumType.STRING)
	@Column(name = "block_type", nullable = false, length = 50)
	private BlockType blockType;

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "input_schema", columnDefinition = "JSON")
	private String inputSchema;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "option_schema", columnDefinition = "JSON")
	private String optionSchema;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "output_schema", columnDefinition = "JSON")
	private String outputSchema;

	@Enumerated(EnumType.STRING)
	@Column(name = "default_execution_mode", nullable = false, length = 30)
	@ColumnDefault("'PRESET'")
	private ExecutionMode defaultExecutionMode = ExecutionMode.PRESET;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	@ColumnDefault("'ACTIVE'")
	private BlockStatus status = BlockStatus.ACTIVE;
}
