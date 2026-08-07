-- =========================================================
-- V3: 카테고리 마스터 데이터 9건 최초 삽입
-- categories.name에는 CategoryCode enum 값을 저장합니다.
-- 한글 표시명은 응답 변환 과정에서 제공합니다.
-- =========================================================

INSERT INTO `categories` (
    `category_id`,
    `name`,
    `description`,
    `sort_order`
)
VALUES
    (1, 'COMMUNITY',        NULL, 1),
    (2, 'RESEARCH',         NULL, 2),
    (3, 'TUTORIAL',         NULL, 3),
    (4, 'DOCUMENT_SUMMARY', NULL, 4),
    (5, 'SUMMARY',          NULL, 5),
    (6, 'WRITING',          NULL, 6),
    (7, 'REVIEW',           NULL, 7),
    (8, 'AI_TOOL',          NULL, 8),
    (9, 'ROUTINE',          NULL, 9);
