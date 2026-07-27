package com.umc.learninglm.domain.flow.entitiy;

import com.umc.learninglm.domain.block.entity.Block;
import com.umc.learninglm.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Getter
@Entity
@Table(name = "prompt_templates")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromptTemplate extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "prompt_template_id", nullable = false)
	private Long promptTemplateId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "block_id")
	private Block block;

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
