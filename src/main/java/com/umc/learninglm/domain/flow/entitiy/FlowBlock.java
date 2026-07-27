package com.umc.learninglm.domain.flow.entitiy;

import com.umc.learninglm.domain.block.entity.Block;
import com.umc.learninglm.domain.block.enums.ExecutionMode;
import com.umc.learninglm.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "flow_blocks")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FlowBlock extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "flow_block_id", nullable = false)
	private Long flowBlockId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "flow_id", nullable = false)
	private Flow flow;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "block_id", nullable = false)
	private Block block;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "prompt_template_id")
	private PromptTemplate promptTemplate;

	@Column(name = "block_order", nullable = false)
	private Integer blockOrder;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "options", columnDefinition = "JSON")
	private String options;

	@Column(name = "input_value", columnDefinition = "TEXT")
	private String inputValue;

	@Column(name = "output_value", columnDefinition = "TEXT")
	private String outputValue;

	@Enumerated(EnumType.STRING)
	@Column(name = "execution_mode", nullable = false, length = 30)
	@ColumnDefault("'PRESET'")
	private ExecutionMode executionMode = ExecutionMode.PRESET;
}
