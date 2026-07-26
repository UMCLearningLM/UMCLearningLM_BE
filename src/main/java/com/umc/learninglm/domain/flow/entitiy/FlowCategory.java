package com.umc.learninglm.domain.flow.entitiy;

import com.umc.learninglm.domain.category.entity.Category;
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

@Getter
@Entity
@Table(name = "flow_categories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FlowCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "flow_category_id", nullable = false)
	private Long flowCategoryId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "flow_id", nullable = false)
	private Flow flow;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;
}
