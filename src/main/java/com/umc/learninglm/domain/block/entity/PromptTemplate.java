package com.umc.learninglm.domain.block.entity;

import com.umc.learninglm.domain.block.enums.BlockType;
import com.umc.learninglm.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@Table(
		name = "prompt_templates",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_prompt_templates_key_version",
				columnNames = {"template_key", "version"}
		)
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromptTemplate extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "prompt_template_id", nullable = false)
	private Long promptTemplateId;

	@Enumerated(EnumType.STRING)
	@Column(name = "stage", nullable = false, length = 50)
	private BlockType stage;

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@Column(name = "template_key", nullable = false, length = 100)
	private String templateKey;

	@Column(name = "prompt_body", nullable = false, columnDefinition = "TEXT")
	private String promptBody;

	@Column(name = "version", nullable = false, length = 30)
	@ColumnDefault("'v1'")
	private String version = "v1";

	@Column(name = "is_active", nullable = false)
	private Boolean active;
}
