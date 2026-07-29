package com.umc.learninglm.domain.tutorial.entity;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// tutorials ↔ categories 다대다(M:N) 조인 테이블
@Getter
@Entity
@Table(
		name = "tutorial_categories",
		uniqueConstraints = @UniqueConstraint(name = "uq_tutorial_category", columnNames = {"tutorial_id", "category_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TutorialCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "tutorial_category_id", nullable = false)
	private Long tutorialCategoryId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tutorial_id", nullable = false)
	private Tutorial tutorial;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;
}
