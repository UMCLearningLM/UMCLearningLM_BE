package com.umc.learninglm.domain.tutorial.entity;

import com.umc.learninglm.domain.tutorial.enums.SavedTutorialStatus;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@Table(
		name = "saved_tutorials",
		uniqueConstraints = @UniqueConstraint(name = "uq_saved_tutorial", columnNames = {"user_id", "tutorial_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavedTutorial extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "saved_tutorial_id", nullable = false)
	private Long savedTutorialId;

	// users 참조 (auth 도메인 → Long FK)
	@Column(name = "user_id", nullable = false)
	private Long userId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tutorial_id", nullable = false)
	private Tutorial tutorial;

	// 학습용 flow (flow 도메인, nullable → Long FK)
	@Column(name = "flow_id")
	private Long flowId;

	@Column(name = "current_step_order", nullable = false)
	@ColumnDefault("1")
	private Integer currentStepOrder = 1;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	@ColumnDefault("'NOT_STARTED'")
	private SavedTutorialStatus status = SavedTutorialStatus.NOT_STARTED;

	private SavedTutorial(Long userId, Tutorial tutorial) {
		this.userId = userId;
		this.tutorial = tutorial;
	}

	// 저장(북마크) 생성 — NOT_STARTED, 1단계
	public static SavedTutorial createBookmark(Long userId, Tutorial tutorial) {
		return new SavedTutorial(userId, tutorial);
	}

	// 학습 시작 — IN_PROGRESS 전환 + flow 연결. 1단계부터 시작.
	public void start(Long flowId) {
		this.flowId = flowId;
		this.currentStepOrder = 1;
		this.status = SavedTutorialStatus.IN_PROGRESS;
	}

	// 진행 단계 갱신
	public void updateStep(int currentStepOrder) {
		this.currentStepOrder = currentStepOrder;
	}

	// 완료 처리
	public void complete(int currentStepOrder) {
		this.currentStepOrder = currentStepOrder;
		this.status = SavedTutorialStatus.COMPLETED;
	}
}
