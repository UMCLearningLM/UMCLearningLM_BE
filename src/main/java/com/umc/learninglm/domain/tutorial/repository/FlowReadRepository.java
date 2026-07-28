package com.umc.learninglm.domain.tutorial.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

// flow 테이블(flows) 읽기 전용 브릿지.
// flows를 엔티티로 매핑하지 않으므로 flow 도메인 정식 엔티티와 충돌하지 않음.
// 추후 flow 도메인이 서비스를 제공하면 그 호출로 대체.
@Repository
@RequiredArgsConstructor
public class FlowReadRepository {

	private final JdbcTemplate jdbcTemplate;

	public Optional<FlowView> findByFlowId(Long flowId) {
		List<FlowView> results = jdbcTemplate.query(
				"SELECT flow_type, example_input, example_result, tutorial_id FROM flows WHERE flow_id = ?",
				(rs, rowNum) -> new FlowView(
						rs.getString("flow_type"),
						rs.getString("example_input"),
						rs.getString("example_result"),
						(Long) rs.getObject("tutorial_id")),
				flowId);
		return results.stream().findFirst();
	}
}
