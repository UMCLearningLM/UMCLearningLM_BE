package com.umc.learninglm.domain.tutorial.entity;

import com.umc.learninglm.domain.block.entity.Block;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

// ERD상 created_at/updated_at 없음 → BaseTimeEntity 미상속
@Getter
@Entity
@Table(
		name = "tutorial_blocks",
		uniqueConstraints = @UniqueConstraint(name = "uq_tutorial_block_order", columnNames = {"tutorial_step_id", "block_order"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TutorialBlock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "tutorial_block_id", nullable = false)
	private Long tutorialBlockId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "block_id", nullable = false)
	private Block block;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tutorial_step_id", nullable = false)
	private TutorialStep tutorialStep;

	@Column(name = "block_order", nullable = false)
	private Integer blockOrder;

	@Column(name = "required", nullable = false)
	private Boolean required;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "default_options")
	private String defaultOptions;

	@Column(name = "reason", length = 255)
	private String reason;
}
