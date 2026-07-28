package com.umc.learninglm.domain.tutorial.entity;

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

@Getter
@Entity
@Table(name = "tutorial_steps")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TutorialStep extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "tutorial_step_id", nullable = false)
	private Long tutorialStepId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tutorial_id", nullable = false)
	private Tutorial tutorial;

	@Column(name = "step_order", nullable = false)
	private Integer stepOrder;

	@Column(name = "title", nullable = false, length = 200)
	private String title;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "guide_text", columnDefinition = "TEXT")
	private String guideText;
}
