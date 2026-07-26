package com.umc.learninglm.domain.flow.entitiy;

import com.umc.learninglm.domain.auth.entity.User;
import com.umc.learninglm.domain.flow.enums.*;
import com.umc.learninglm.domain.tutorial.entity.Tutorial;
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

@Getter
@Entity
@Table(name = "flows")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Flow extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "flow_id", nullable = false)
	private Long flowId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "origin_flow_id")
	private Flow originFlow;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tutorial_id")
	private Tutorial tutorial;

	@Column(name = "title", nullable = false, length = 200)
	private String title;

	@Column(name = "summary", columnDefinition = "TEXT")
	private String summary;

	@Column(name = "purpose", columnDefinition = "TEXT")
	private String purpose;

	@Enumerated(EnumType.STRING)
	@Column(name = "difficulty", nullable = false, length = 30)
	private FlowDifficulty difficulty;

	@Enumerated(EnumType.STRING)
	@Column(name = "visibility", nullable = false, length = 30)
	@ColumnDefault("'PRIVATE'")
	private FlowVisibility visibility = FlowVisibility.PRIVATE;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	@ColumnDefault("'DRAFT'")
	private FlowStatus status = FlowStatus.DRAFT;

	@Column(name = "example_input", columnDefinition = "TEXT")
	private String exampleInput;

	@Column(name = "example_result", columnDefinition = "TEXT")
	private String exampleResult;

	@Column(name = "author_note", columnDefinition = "TEXT")
	private String authorNote;

	@Enumerated(EnumType.STRING)
	@Column(name = "flow_type", nullable = false, length = 30)
	@ColumnDefault("'USER'")
	private FlowType flowType = FlowType.USER;

	@Enumerated(EnumType.STRING)
	@Column(name = "mode", nullable = false, length = 30)
	private FlowMode mode;
}
