package com.umc.learninglm.domain.flow.entity;

import com.umc.learninglm.domain.category.entity.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "flow_categories",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_flow_category",
                        columnNames = {"flow_id", "category_id"}
                )
        }
)
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
