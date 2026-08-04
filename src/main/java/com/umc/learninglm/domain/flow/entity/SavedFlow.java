package com.umc.learninglm.domain.flow.entity;

import com.umc.learninglm.domain.auth.entity.User;
import com.umc.learninglm.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "saved_flows",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_saved_flow",
                        columnNames = {"user_id", "flow_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SavedFlow extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "saved_flow_id", nullable = false)
	private Long savedFlowId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "flow_id", nullable = false)
	private Flow flow;
}
