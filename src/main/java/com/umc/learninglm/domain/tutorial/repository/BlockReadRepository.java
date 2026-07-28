package com.umc.learninglm.domain.tutorial.repository;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

// block 테이블(blocks) 읽기 전용 브릿지.
// blocks를 엔티티로 매핑하지 않으므로 block 도메인 정식 엔티티와 충돌하지 않음.
// 추후 block 도메인이 서비스를 제공하면 그 호출로 대체.
@Repository
@RequiredArgsConstructor
public class BlockReadRepository {

	private final JdbcTemplate jdbcTemplate;

	public List<BlockView> findAllByBlockIds(Collection<Long> blockIds) {
		if (blockIds.isEmpty()) {
			return List.of();
		}
		String placeholders = blockIds.stream().map(id -> "?").collect(Collectors.joining(","));
		return jdbcTemplate.query(
				"SELECT block_id, name, block_type, description FROM blocks WHERE block_id IN (" + placeholders + ")",
				(rs, rowNum) -> new BlockView(
						rs.getLong("block_id"),
						rs.getString("name"),
						rs.getString("block_type"),
						rs.getString("description")),
				blockIds.toArray());
	}
}
