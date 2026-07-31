package com.umc.learninglm.domain.library.repository;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LibraryQueryRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    // 검색 및 필터 조건에 맞는 공개 흐름 목록을 조회
    public List<FlowSummaryRow> findPublicFlows(
            String keyword,
            List<Long> categoryIds,
            List<String> difficulties,
            Long userId
    ) {
        StringBuilder sql = new StringBuilder("""
            SELECT
                f.flow_id,
                f.user_id AS author_id,
                u.nickname AS author_nickname,
                f.title,
                f.summary,
                f.difficulty,
                (
                    SELECT COUNT(*)
                    FROM flow_likes fl
                    WHERE fl.flow_id = f.flow_id
                ) AS like_count,
                (
                    SELECT COUNT(*)
                    FROM flows copied
                    WHERE copied.origin_flow_id = f.flow_id
                ) AS copy_count,
                (
                    SELECT COUNT(*)
                    FROM flow_comments fc
                    WHERE fc.flow_id = f.flow_id
                      AND fc.status = 'ACTIVE'
                ) AS comment_count,
                EXISTS (
                    SELECT 1
                    FROM flow_likes my_like
                    WHERE my_like.flow_id = f.flow_id
                      AND my_like.user_id = :userId
                ) AS is_liked
            FROM flows f
            JOIN users u
              ON u.user_id = f.user_id
            WHERE f.visibility = 'PUBLIC'
              AND f.status = 'COMPLETED'
            """);

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId, Types.BIGINT);

        if (keyword != null) {
            sql.append("""
                  AND (
                      LOWER(f.title) LIKE :keyword
                      OR LOWER(COALESCE(f.summary, '')) LIKE :keyword
                      OR EXISTS (
                          SELECT 1
                          FROM flow_tags ft
                          JOIN tags t
                            ON t.tag_id = ft.tag_id
                          WHERE ft.flow_id = f.flow_id
                            AND LOWER(t.name) LIKE :keyword
                      )
                  )
                """);

            params.addValue(
                    "keyword",
                    "%" + keyword.toLowerCase() + "%"
            );
        }

        if (!categoryIds.isEmpty()) {
            sql.append("""
                  AND EXISTS (
                      SELECT 1
                      FROM flow_categories filter_fc
                      WHERE filter_fc.flow_id = f.flow_id
                        AND filter_fc.category_id IN (:categoryIds)
                  )
                """);

            params.addValue("categoryIds", categoryIds);
        }

        if (!difficulties.isEmpty()) {
            sql.append("""
                  AND f.difficulty IN (:difficulties)
                """);

            params.addValue("difficulties", difficulties);
        }

        sql.append("""
            ORDER BY f.created_at DESC, f.flow_id DESC
            """);

        return jdbcTemplate.query(
                sql.toString(),
                params,
                (resultSet, rowNumber) -> new FlowSummaryRow(
                        resultSet.getLong("flow_id"),
                        resultSet.getLong("author_id"),
                        resultSet.getString("author_nickname"),
                        resultSet.getString("title"),
                        resultSet.getString("summary"),
                        resultSet.getString("difficulty"),
                        resultSet.getLong("like_count"),
                        resultSet.getLong("copy_count"),
                        resultSet.getLong("comment_count"),
                        resultSet.getBoolean("is_liked")
                )
        );
    }

    // 공개 완료 상태의 흐름 상세 정보를 조회
    public Optional<FlowDetailRow> findPublicFlowDetail(
            Long flowId,
            Long userId
    ) {
        String sql = """
            SELECT
                f.flow_id,
                f.user_id AS author_id,
                u.nickname AS author_nickname,
                f.title,
                f.summary,
                f.difficulty,
                f.example_input,
                f.example_result,
                f.author_note,
                (
                    SELECT COUNT(*)
                    FROM flow_likes fl
                    WHERE fl.flow_id = f.flow_id
                ) AS like_count,
                (
                    SELECT COUNT(*)
                    FROM flows copied
                    WHERE copied.origin_flow_id = f.flow_id
                ) AS copy_count,
                (
                    SELECT COUNT(*)
                    FROM flow_bookmarks fb
                    WHERE fb.flow_id = f.flow_id
                ) AS bookmark_count,
                (
                    SELECT COUNT(*)
                    FROM flow_comments fc
                    WHERE fc.flow_id = f.flow_id
                      AND fc.status = 'ACTIVE'
                ) AS comment_count,
                EXISTS (
                    SELECT 1
                    FROM flow_likes my_like
                    WHERE my_like.flow_id = f.flow_id
                      AND my_like.user_id = :userId
                ) AS is_liked,
                EXISTS (
                    SELECT 1
                    FROM flow_bookmarks my_bookmark
                    WHERE my_bookmark.flow_id = f.flow_id
                      AND my_bookmark.user_id = :userId
                ) AS is_bookmarked
            FROM flows f
            JOIN users u
              ON u.user_id = f.user_id
            WHERE f.flow_id = :flowId
              AND f.visibility = 'PUBLIC'
              AND f.status = 'COMPLETED'
            """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("flowId", flowId)
                .addValue("userId", userId, Types.BIGINT);

        return jdbcTemplate.query(
                        sql,
                        params,
                        (resultSet, rowNumber) -> new FlowDetailRow(
                                resultSet.getLong("flow_id"),
                                resultSet.getLong("author_id"),
                                resultSet.getString("author_nickname"),
                                resultSet.getString("title"),
                                resultSet.getString("summary"),
                                resultSet.getString("difficulty"),
                                resultSet.getString("example_input"),
                                resultSet.getString("example_result"),
                                resultSet.getString("author_note"),
                                resultSet.getLong("like_count"),
                                resultSet.getLong("copy_count"),
                                resultSet.getLong("bookmark_count"),
                                resultSet.getLong("comment_count"),
                                resultSet.getBoolean("is_liked"),
                                resultSet.getBoolean("is_bookmarked")
                        )
                )
                .stream()
                .findFirst();
    }

    // 여러 흐름의 카테고리를 정렬 순서대로 조회
    public List<CategoryRow> findCategoriesByFlowIds(
            List<Long> flowIds
    ) {
        if (flowIds.isEmpty()) {
            return List.of();
        }

        String sql = """
                SELECT
                    fc.flow_id,
                    c.category_id,
                    c.name AS category_code
                FROM flow_categories fc
                JOIN categories c
                  ON c.category_id = fc.category_id
                WHERE fc.flow_id IN (:flowIds)
                ORDER BY
                    fc.flow_id ASC,
                    c.sort_order ASC,
                    c.category_id ASC
                """;

        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("flowIds", flowIds),
                (resultSet, rowNumber) -> new CategoryRow(
                        resultSet.getLong("flow_id"),
                        resultSet.getLong("category_id"),
                        resultSet.getString("category_code")
                )
        );
    }

    // 상세 화면에 표시할 태그를 조회
    public List<TagRow> findTagsByFlowId(Long flowId) {
        String sql = """
                SELECT
                    t.tag_id,
                    t.name
                FROM flow_tags ft
                JOIN tags t
                  ON t.tag_id = ft.tag_id
                WHERE ft.flow_id = :flowId
                ORDER BY ft.flow_tag_id ASC
                """;

        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("flowId", flowId),
                (resultSet, rowNumber) -> new TagRow(
                        resultSet.getLong("tag_id"),
                        resultSet.getString("name")
                )
        );
    }

    // 상세 화면에 표시할 블록 흐름을 순서대로 조회
    public List<FlowBlockRow> findBlocksByFlowId(Long flowId) {
        String sql = """
                SELECT
                    fb.flow_block_id,
                    b.block_id,
                    b.name,
                    b.block_type AS stage,
                    fb.block_order
                FROM flow_blocks fb
                JOIN blocks b
                  ON b.block_id = fb.block_id
                WHERE fb.flow_id = :flowId
                ORDER BY fb.block_order ASC, fb.flow_block_id ASC
                """;

        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("flowId", flowId),
                (resultSet, rowNumber) -> new FlowBlockRow(
                        resultSet.getLong("flow_block_id"),
                        resultSet.getLong("block_id"),
                        resultSet.getString("name"),
                        resultSet.getString("stage"),
                        resultSet.getInt("block_order")
                )
        );
    }

    // 상세 화면에 표시할 활성 댓글 전체를 최신순으로 조회
    public List<CommentRow> findActiveCommentsByFlowId(Long flowId) {
        String sql = """
            SELECT
                fc.flow_comment_id,
                u.user_id,
                u.nickname,
                fc.content,
                fc.created_at
            FROM flow_comments fc
            JOIN users u
              ON u.user_id = fc.user_id
            WHERE fc.flow_id = :flowId
              AND fc.status = 'ACTIVE'
            ORDER BY
                fc.created_at DESC,
                fc.flow_comment_id DESC
            """;

        return jdbcTemplate.query(
                sql,
                new MapSqlParameterSource("flowId", flowId),
                (resultSet, rowNumber) -> new CommentRow(
                        resultSet.getLong("flow_comment_id"),
                        resultSet.getLong("user_id"),
                        resultSet.getString("nickname"),
                        resultSet.getString("content"),
                        toLocalDateTime(
                                resultSet.getTimestamp("created_at")
                        )
                )
        );
    }

    // 요청된 카테고리 식별자가 실제로 존재하는지 확인
    public long countExistingCategories(List<Long> categoryIds) {
        if (categoryIds.isEmpty()) {
            return 0;
        }

        String sql = """
                SELECT COUNT(DISTINCT c.category_id)
                FROM categories c
                WHERE c.category_id IN (:categoryIds)
                """;

        Long count = jdbcTemplate.queryForObject(
                sql,
                new MapSqlParameterSource(
                        "categoryIds",
                        categoryIds
                ),
                Long.class
        );

        return count == null ? 0 : count;
    }

    // SQL Timestamp를 LocalDateTime으로 변환
    private LocalDateTime toLocalDateTime(
            java.sql.Timestamp timestamp
    ) {
        return timestamp == null
                ? null
                : timestamp.toLocalDateTime();
    }

    public record FlowSummaryRow(
            Long flowId,
            Long authorId,
            String authorNickname,
            String title,
            String summary,
            String difficulty,
            long likeCount,
            long copyCount,
            long commentCount,
            boolean isLiked
    ) {
    }

    public record FlowDetailRow(
            Long flowId,
            Long authorId,
            String authorNickname,
            String title,
            String summary,
            String difficulty,
            String exampleInput,
            String exampleResult,
            String authorNote,
            long likeCount,
            long copyCount,
            long bookmarkCount,
            long commentCount,
            boolean isLiked,
            boolean isBookmarked
    ) {
    }

    public record CategoryRow(
            Long flowId,
            Long categoryId,
            String categoryCode
    ) {
    }

    public record TagRow(
            Long tagId,
            String name
    ) {
    }

    public record FlowBlockRow(
            Long flowBlockId,
            Long blockId,
            String name,
            String stage,
            int blockOrder
    ) {
    }

    public record CommentRow(
            Long commentId,
            Long userId,
            String nickname,
            String content,
            LocalDateTime createdAt
    ) {
    }
}