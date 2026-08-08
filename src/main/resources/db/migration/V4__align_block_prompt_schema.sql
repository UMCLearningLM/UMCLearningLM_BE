-- Align the merged Flow schema with the Block-owned prompt template model.
ALTER TABLE `blocks`
    ADD COLUMN `block_key` VARCHAR(30) NULL AFTER `block_id`,
    ADD COLUMN `prompt_instruction` TEXT NULL AFTER `description`,
    ADD COLUMN `prompt_template_id` BIGINT NULL AFTER `prompt_instruction`;

ALTER TABLE `prompt_templates`
    ADD COLUMN `stage` VARCHAR(50) NULL AFTER `prompt_template_id`;

UPDATE `prompt_templates` pt
JOIN `blocks` b ON b.`block_id` = pt.`block_id`
SET pt.`stage` = b.`block_type`
WHERE pt.`stage` IS NULL;

UPDATE `prompt_templates`
SET `stage` = 'PROCESS'
WHERE `stage` IS NULL;

ALTER TABLE `prompt_templates`
    MODIFY COLUMN `stage` VARCHAR(50) NOT NULL,
    ADD CONSTRAINT `uk_prompt_templates_key_version`
        UNIQUE (`template_key`, `version`);

UPDATE `blocks` b
SET b.`block_key` = CONCAT(
        CASE b.`block_type`
            WHEN 'INPUT' THEN 'IN'
            WHEN 'CONTEXT' THEN 'CTX'
            WHEN 'PROCESS' THEN 'PR'
            WHEN 'REVIEW' THEN 'RV'
            WHEN 'OUTPUT' THEN 'OUT'
        END,
        '-',
        LPAD(
            CASE b.`block_type`
                WHEN 'INPUT' THEN b.`block_id`
                WHEN 'CONTEXT' THEN b.`block_id` - 9
                WHEN 'PROCESS' THEN b.`block_id` - 16
                WHEN 'REVIEW' THEN b.`block_id` - 32
                WHEN 'OUTPUT' THEN b.`block_id` - 41
            END,
            3,
            '0'
        )
    ),
    b.`prompt_instruction` = COALESCE(
        NULLIF(b.`prompt_instruction`, ''),
        NULLIF(b.`description`, ''),
        b.`name`
    )
WHERE b.`block_key` IS NULL;

ALTER TABLE `blocks`
    MODIFY COLUMN `block_key` VARCHAR(30) NOT NULL,
    ADD CONSTRAINT `uk_blocks_block_key` UNIQUE (`block_key`),
    ADD INDEX `idx_blocks_prompt_template_id` (`prompt_template_id`);

INSERT INTO `prompt_templates` (
    `stage`,
    `name`,
    `template_key`,
    `prompt_body`,
    `version`,
    `is_active`,
    `created_at`,
    `updated_at`
)
VALUES
    ('INPUT', '입력 단계 기본 템플릿', 'STAGE_INPUT_DEFAULT',
     '{blockInstruction}\n입력 설정: {options}', 'v1', TRUE, NOW(), NOW()),
    ('CONTEXT', '컨텍스트 단계 기본 템플릿', 'STAGE_CONTEXT_DEFAULT',
     '{blockInstruction}\n컨텍스트 설정: {options}', 'v1', TRUE, NOW(), NOW()),
    ('PROCESS', '처리 단계 기본 템플릿', 'STAGE_PROCESS_DEFAULT',
     '{blockInstruction}\n처리 설정: {options}', 'v1', TRUE, NOW(), NOW()),
    ('REVIEW', '검토 단계 기본 템플릿', 'STAGE_REVIEW_DEFAULT',
     '{blockInstruction}\n검토 설정: {options}', 'v1', TRUE, NOW(), NOW()),
    ('OUTPUT', '출력 단계 기본 템플릿', 'STAGE_OUTPUT_DEFAULT',
     '{blockInstruction}\n출력 설정: {options}', 'v1', TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `prompt_body` = VALUES(`prompt_body`),
    `is_active` = VALUES(`is_active`),
    `updated_at` = VALUES(`updated_at`);

UPDATE `blocks` b
JOIN `prompt_templates` pt
    ON pt.`block_id` = b.`block_id`
    AND pt.`is_active` = TRUE
SET b.`prompt_template_id` = pt.`prompt_template_id`
WHERE b.`prompt_template_id` IS NULL;

UPDATE `blocks` b
JOIN `prompt_templates` pt
    ON pt.`template_key` = CASE b.`block_type`
        WHEN 'INPUT' THEN 'STAGE_INPUT_DEFAULT'
        WHEN 'CONTEXT' THEN 'STAGE_CONTEXT_DEFAULT'
        WHEN 'PROCESS' THEN 'STAGE_PROCESS_DEFAULT'
        WHEN 'REVIEW' THEN 'STAGE_REVIEW_DEFAULT'
        WHEN 'OUTPUT' THEN 'STAGE_OUTPUT_DEFAULT'
    END
    AND pt.`version` = 'v1'
    AND pt.`is_active` = TRUE
SET b.`prompt_template_id` = pt.`prompt_template_id`
WHERE b.`prompt_template_id` IS NULL;

ALTER TABLE `blocks`
    ADD CONSTRAINT `fk_blocks_prompt_template`
        FOREIGN KEY (`prompt_template_id`)
        REFERENCES `prompt_templates` (`prompt_template_id`)
        ON DELETE SET NULL;