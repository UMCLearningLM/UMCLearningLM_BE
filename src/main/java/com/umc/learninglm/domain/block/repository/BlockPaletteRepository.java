package com.umc.learninglm.domain.block.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BlockPaletteRepository {

    private static final String BLOCK_ORDER_SQL = """
            ORDER BY FIELD(
                b.block_type,
                'INPUT',
                'CONTEXT',
                'PROCESS',
                'REVIEW',
                'OUTPUT'
            ),
            b.block_id ASC
            """;

    private final JdbcTemplate jdbcTemplate;

    // 자유 제작 모드에서 사용할 수 있는 전체 블록을 조회
    public List<BlockPaletteView> findCreateModeBlocks(
            String keyword,
            String stage
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    b.block_id,
                    b.block_type AS stage,
                    b.name,
                    b.description,
                    b.status
                FROM blocks b
                WHERE b.status IN ('ACTIVE', 'PLACEHOLDER')
                """);

        List<Object> parameters = new ArrayList<>();

        appendSearchConditions(
                sql,
                parameters,
                keyword,
                stage
        );

        sql.append(BLOCK_ORDER_SQL);

        return jdbcTemplate.query(
                sql.toString(),
                (resultSet, rowNum) -> new BlockPaletteView(
                        resultSet.getLong("block_id"),
                        resultSet.getString("stage"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getString("status"),
                        null
                ),
                parameters.toArray()
        );
    }

    // 튜토리얼 모드에서 해당 튜토리얼에 설정된 블록을 조회
    public List<BlockPaletteView> findTutorialModeBlocks(
            Long tutorialId,
            String keyword,
            String stage
    ) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    b.block_id,
                    b.block_type AS stage,
                    b.name,
                    b.description,
                    b.status,
                    CASE
                        WHEN MAX(
                            CASE
                                WHEN tb.required = TRUE THEN 1
                                ELSE 0
                            END
                        ) = 1
                        THEN 1
                        ELSE 0
                    END AS required
                FROM tutorial_steps ts
                INNER JOIN tutorial_blocks tb
                    ON tb.tutorial_step_id = ts.tutorial_step_id
                INNER JOIN blocks b
                    ON b.block_id = tb.block_id
                WHERE ts.tutorial_id = ?
                  AND b.status IN ('ACTIVE', 'PLACEHOLDER')
                """);

        List<Object> parameters = new ArrayList<>();
        parameters.add(tutorialId);

        appendSearchConditions(
                sql,
                parameters,
                keyword,
                stage
        );

        sql.append("""
                GROUP BY
                    b.block_id,
                    b.block_type,
                    b.name,
                    b.description,
                    b.status
                """);

        sql.append(BLOCK_ORDER_SQL);

        return jdbcTemplate.query(
                sql.toString(),
                (resultSet, rowNum) -> new BlockPaletteView(
                        resultSet.getLong("block_id"),
                        resultSet.getString("stage"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getString("status"),
                        resultSet.getInt("required") == 1
                ),
                parameters.toArray()
        );
    }

    // 공개된 튜토리얼인지 확인
    public boolean existsPublishedTutorial(Long tutorialId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM tutorials
                WHERE tutorial_id = ?
                  AND status = 'PUBLISHED'
                """,
                Integer.class,
                tutorialId
        );

        return count != null && count > 0;
    }

    private void appendSearchConditions(
            StringBuilder sql,
            List<Object> parameters,
            String keyword,
            String stage
    ) {
        if (stage != null) {
            sql.append("""
                      AND b.block_type = ?
                    """);
            parameters.add(stage);
        }

        if (keyword != null) {
            String likeKeyword =
                    "%" + keyword.toLowerCase(Locale.ROOT) + "%";

            sql.append("""
                      AND (
                          LOWER(b.name) LIKE ?
                          OR LOWER(COALESCE(b.description, '')) LIKE ?
                      )
                    """);

            parameters.add(likeKeyword);
            parameters.add(likeKeyword);
        }
    }
}