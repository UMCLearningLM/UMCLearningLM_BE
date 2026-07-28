package com.umc.learninglm.domain.tutorial.entity;

import com.umc.learninglm.domain.tutorial.enums.Difficulty;
import com.umc.learninglm.domain.tutorial.enums.TutorialStatus;
import com.umc.learninglm.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "tutorials")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tutorial extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "tutorial_id", nullable = false)
	private Long tutorialId;

	@Column(name = "title", nullable = false, length = 200)
	private String title;

	// 예시 입력·결과 보여주기용 flow (flow 도메인 → Long FK)
	@Column(name = "preset_flow_id")
	private Long presetFlowId;

	@Column(name = "summary", columnDefinition = "TEXT")
	private String summary;

	@Enumerated(EnumType.STRING)
	@Column(name = "difficulty", nullable = false, length = 30)
	private Difficulty difficulty;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 30)
	@ColumnDefault("'PUBLISHED'")
	private TutorialStatus status = TutorialStatus.PUBLISHED;

	@Column(name = "thumbnail_url", length = 500)
	private String thumbnailUrl;

	@Column(name = "estimated_minutes")
	private Integer estimatedMinutes;

	// JSON 컬럼: 원본 JSON 문자열로 보관 (서비스에서 Jackson으로 파싱). 필요 시 List/Map 타입으로 교체 가능.
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "use_cases")
	private String useCases;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "required_concepts")
	private String requiredConcepts;
}
