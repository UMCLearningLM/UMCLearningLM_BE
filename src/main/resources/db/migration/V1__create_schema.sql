-- =========================================================
-- V1: LearningLM 전체 스키마 생성
-- MySQL 8.x / InnoDB / utf8mb4
-- =========================================================

SET NAMES utf8mb4;

CREATE TABLE `users` (
    `user_id` BIGINT NOT NULL AUTO_INCREMENT,
    `email` VARCHAR(255) NOT NULL,
    `password_hash` VARCHAR(255) NULL,
    `nickname` VARCHAR(50) NOT NULL,
    `provider_id` VARCHAR(255) NULL,
    `provider` VARCHAR(30) NOT NULL DEFAULT 'LOCAL' COMMENT 'LOCAL / GOOGLE',
    `role` VARCHAR(30) NOT NULL DEFAULT 'USER' COMMENT 'USER / ADMIN',
    `status` VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / INACTIVE',
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    CONSTRAINT `pk_users` PRIMARY KEY (`user_id`),
    CONSTRAINT `uk_users_email` UNIQUE (`email`),
    CONSTRAINT `uk_users_provider_provider_id` UNIQUE (`provider`, `provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `token_code` (
    `token_code_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '토큰 코드 식별자',
    `user_id` BIGINT NULL,
    `email` VARCHAR(255) NULL COMMENT '비로그인 이메일 인증 대상 이메일',
    `token_hash` VARCHAR(255) NOT NULL COMMENT '토큰 해시값',
    `type` VARCHAR(30) NOT NULL COMMENT 'REFRESH / ACCESS_BLACKLIST / EMAIL_VERIFICATION / EMAIL_CODE',
    `purpose` VARCHAR(30) NULL COMMENT 'SIGNUP / PASSWORD_RESET / EMAIL_CHANGE',
    `status` VARCHAR(30) NOT NULL COMMENT 'ACTIVE / USED / REVOKED / EXPIRED',
    `exp_time` DATETIME(6) NOT NULL COMMENT '토큰 만료 일시',
    `created_at` DATETIME(6) NOT NULL COMMENT '생성 일시',
    `used_at` DATETIME(6) NULL COMMENT '사용 완료 일시',
    `revoked_at` DATETIME(6) NULL COMMENT '폐기 일시',
    `attempt_count` INT NOT NULL COMMENT '이메일 인증코드 검증 실패 횟수',
    CONSTRAINT `pk_token_code` PRIMARY KEY (`token_code_id`),
    INDEX `idx_token_code_user_id` (`user_id`),
    INDEX `idx_token_code_token_hash` (`token_hash`),
    INDEX `idx_token_code_email_type_purpose_status` (`email`, `type`, `purpose`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `categories` (
    `category_id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT 'COMMUNITY / RESEARCH / TUTORIAL / DOCUMENT_SUMMARY / SUMMARY / WRITING / REVIEW / AI_TOOL / ROUTINE',
    `description` TEXT NULL,
    `sort_order` INT NOT NULL,
    CONSTRAINT `pk_categories` PRIMARY KEY (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `blocks` (
    `block_id` BIGINT NOT NULL AUTO_INCREMENT,
    `block_type` VARCHAR(50) NOT NULL COMMENT 'INPUT / CONTEXT / PROCESS / REVIEW / OUTPUT',
    `name` VARCHAR(100) NOT NULL,
    `description` TEXT NULL,
    `input_schema` JSON NULL,
    `option_schema` JSON NULL,
    `output_schema` JSON NULL,
    `default_execution_mode` VARCHAR(30) NOT NULL DEFAULT 'PRESET' COMMENT 'PRESET',
    `status` VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / PLACEHOLDER / INACTIVE',
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    CONSTRAINT `pk_blocks` PRIMARY KEY (`block_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tags` (
    `tag_id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    CONSTRAINT `pk_tags` PRIMARY KEY (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- tutorials와 flows는 서로 선택 참조하므로 FK는 전체 테이블 생성 후 추가합니다.
CREATE TABLE `tutorials` (
    `tutorial_id` BIGINT NOT NULL AUTO_INCREMENT,
    `preset_flow_id` BIGINT NULL COMMENT '예시 입력·결과 표시용 flow',
    `title` VARCHAR(200) NOT NULL,
    `summary` TEXT NULL,
    `difficulty` VARCHAR(30) NOT NULL COMMENT 'BEGINNER / BASIC / ADVANCED',
    `status` VARCHAR(30) NOT NULL DEFAULT 'PUBLISHED' COMMENT 'PUBLISHED / DRAFT',
    `thumbnail_url` VARCHAR(500) NULL,
    `estimated_minutes` INT NULL,
    `use_cases` JSON NULL,
    `required_concepts` JSON NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    CONSTRAINT `pk_tutorials` PRIMARY KEY (`tutorial_id`),
    INDEX `idx_tutorials_preset_flow_id` (`preset_flow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `flows` (
    `flow_id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `origin_flow_id` BIGINT NULL,
    `tutorial_id` BIGINT NULL,
    `title` VARCHAR(200) NOT NULL,
    `summary` TEXT NULL,
    `purpose` TEXT NULL,
    `difficulty` VARCHAR(30) NOT NULL COMMENT 'BEGINNER / BASIC / ADVANCED',
    `visibility` VARCHAR(30) NOT NULL DEFAULT 'PRIVATE' COMMENT 'PRIVATE / PUBLIC',
    `status` VARCHAR(30) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT / COMPLETED',
    `example_input` TEXT NULL,
    `example_result` TEXT NULL,
    `author_note` TEXT NULL,
    `flow_type` VARCHAR(30) NOT NULL DEFAULT 'USER' COMMENT 'USER / PRESET',
    `mode` VARCHAR(30) NOT NULL COMMENT 'GUIDED / CREATE',
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    CONSTRAINT `pk_flows` PRIMARY KEY (`flow_id`),
    INDEX `idx_flows_user_id` (`user_id`),
    INDEX `idx_flows_origin_flow_id` (`origin_flow_id`),
    INDEX `idx_flows_tutorial_id` (`tutorial_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `prompt_templates` (
    `prompt_template_id` BIGINT NOT NULL AUTO_INCREMENT,
    `block_id` BIGINT NULL,
    `name` VARCHAR(100) NOT NULL,
    `template_key` VARCHAR(100) NOT NULL,
    `prompt_body` TEXT NOT NULL,
    `version` VARCHAR(30) NOT NULL DEFAULT 'v1',
    `is_active` BOOLEAN NOT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    CONSTRAINT `pk_prompt_templates` PRIMARY KEY (`prompt_template_id`),
    INDEX `idx_prompt_templates_block_id` (`block_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tutorial_steps` (
    `tutorial_step_id` BIGINT NOT NULL AUTO_INCREMENT,
    `tutorial_id` BIGINT NOT NULL,
    `step_order` INT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `description` TEXT NULL,
    `guide_text` TEXT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    CONSTRAINT `pk_tutorial_steps` PRIMARY KEY (`tutorial_step_id`),
    CONSTRAINT `uq_tutorial_step_order` UNIQUE (`tutorial_id`, `step_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `flow_blocks` (
    `flow_block_id` BIGINT NOT NULL AUTO_INCREMENT,
    `flow_id` BIGINT NOT NULL,
    `block_id` BIGINT NOT NULL,
    `prompt_template_id` BIGINT NULL,
    `block_order` INT NOT NULL,
    `options` JSON NULL,
    `input_value` TEXT NULL,
    `output_value` TEXT NULL,
    `execution_mode` VARCHAR(30) NOT NULL DEFAULT 'PRESET' COMMENT 'PRESET',
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    CONSTRAINT `pk_flow_blocks` PRIMARY KEY (`flow_block_id`),
    INDEX `idx_flow_blocks_flow_id` (`flow_id`),
    INDEX `idx_flow_blocks_block_id` (`block_id`),
    INDEX `idx_flow_blocks_prompt_template_id` (`prompt_template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `flow_categories` (
    `flow_category_id` BIGINT NOT NULL AUTO_INCREMENT,
    `flow_id` BIGINT NOT NULL,
    `category_id` BIGINT NOT NULL,
    CONSTRAINT `pk_flow_categories` PRIMARY KEY (`flow_category_id`),
    INDEX `idx_flow_categories_flow_id` (`flow_id`),
    INDEX `idx_flow_categories_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `flow_comments` (
    `flow_comment_id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `flow_id` BIGINT NOT NULL,
    `content` TEXT NOT NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE / INACTIVE',
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    CONSTRAINT `pk_flow_comments` PRIMARY KEY (`flow_comment_id`),
    INDEX `idx_flow_comments_user_id` (`user_id`),
    INDEX `idx_flow_comments_flow_id` (`flow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `saved_tutorials` (
    `saved_tutorial_id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `tutorial_id` BIGINT NOT NULL,
    `flow_id` BIGINT NULL,
    `current_step_order` INT NOT NULL DEFAULT 1,
    `status` VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED' COMMENT 'NOT_STARTED / IN_PROGRESS / COMPLETED',
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    CONSTRAINT `pk_saved_tutorials` PRIMARY KEY (`saved_tutorial_id`),
    CONSTRAINT `uq_saved_tutorial` UNIQUE (`user_id`, `tutorial_id`),
    INDEX `idx_saved_tutorials_tutorial_id` (`tutorial_id`),
    INDEX `idx_saved_tutorials_flow_id` (`flow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `flow_bookmarks` (
    `flow_bookmark_id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `flow_id` BIGINT NOT NULL,
    `created_at` DATETIME(6) NOT NULL,
    CONSTRAINT `pk_flow_bookmarks` PRIMARY KEY (`flow_bookmark_id`),
    INDEX `idx_flow_bookmarks_user_id` (`user_id`),
    INDEX `idx_flow_bookmarks_flow_id` (`flow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tutorial_categories` (
    `tutorial_category_id` BIGINT NOT NULL AUTO_INCREMENT,
    `tutorial_id` BIGINT NOT NULL,
    `category_id` BIGINT NOT NULL,
    CONSTRAINT `pk_tutorial_categories` PRIMARY KEY (`tutorial_category_id`),
    CONSTRAINT `uq_tutorial_category` UNIQUE (`tutorial_id`, `category_id`),
    INDEX `idx_tutorial_categories_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `saved_flows` (
    `saved_flow_id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `flow_id` BIGINT NOT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    CONSTRAINT `pk_saved_flows` PRIMARY KEY (`saved_flow_id`),
    INDEX `idx_saved_flows_user_id` (`user_id`),
    INDEX `idx_saved_flows_flow_id` (`flow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `flow_likes` (
    `flow_like_id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `flow_id` BIGINT NOT NULL,
    `created_at` DATETIME(6) NOT NULL,
    CONSTRAINT `pk_flow_likes` PRIMARY KEY (`flow_like_id`),
    INDEX `idx_flow_likes_user_id` (`user_id`),
    INDEX `idx_flow_likes_flow_id` (`flow_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `tutorial_blocks` (
    `tutorial_block_id` BIGINT NOT NULL AUTO_INCREMENT,
    `block_id` BIGINT NOT NULL,
    `tutorial_step_id` BIGINT NOT NULL,
    `block_order` INT NOT NULL,
    `required` BOOLEAN NOT NULL,
    `default_options` JSON NULL,
    `reason` VARCHAR(255) NULL,
    CONSTRAINT `pk_tutorial_blocks` PRIMARY KEY (`tutorial_block_id`),
    CONSTRAINT `uq_tutorial_block_order` UNIQUE (`tutorial_step_id`, `block_order`),
    INDEX `idx_tutorial_blocks_block_id` (`block_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `ai_execution_logs` (
    `ai_execution_log_id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NULL,
    `flow_id` BIGINT NULL,
    `flow_block_id` BIGINT NULL,
    `provider` VARCHAR(50) NULL,
    `model_name` VARCHAR(100) NULL,
    `request_type` VARCHAR(50) NULL,
    `status` VARCHAR(30) NOT NULL,
    `error_message` TEXT NULL,
    `input_snapshot` JSON NULL,
    `output_snapshot` JSON NULL,
    `token_usage` INT NULL,
    `created_at` DATETIME(6) NOT NULL,
    CONSTRAINT `pk_ai_execution_logs` PRIMARY KEY (`ai_execution_log_id`),
    INDEX `idx_ai_execution_logs_user_id` (`user_id`),
    INDEX `idx_ai_execution_logs_flow_id` (`flow_id`),
    INDEX `idx_ai_execution_logs_flow_block_id` (`flow_block_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `flow_tags` (
    `flow_tag_id` BIGINT NOT NULL AUTO_INCREMENT,
    `flow_id` BIGINT NOT NULL,
    `tag_id` BIGINT NOT NULL,
    CONSTRAINT `pk_flow_tags` PRIMARY KEY (`flow_tag_id`),
    CONSTRAINT `uq_flow_tag` UNIQUE (`flow_id`, `tag_id`),
    INDEX `idx_flow_tags_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- Foreign Keys
-- 선택 참조는 SET NULL, 소유 관계의 하위 데이터는 CASCADE,
-- 마스터/필수 부모 참조는 RESTRICT로 구성합니다.
-- =========================================================

ALTER TABLE `token_code`
    ADD CONSTRAINT `fk_token_code_user`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
        ON DELETE SET NULL;

ALTER TABLE `tutorials`
    ADD CONSTRAINT `fk_tutorials_preset_flow`
        FOREIGN KEY (`preset_flow_id`) REFERENCES `flows` (`flow_id`)
        ON DELETE SET NULL;

ALTER TABLE `flows`
    ADD CONSTRAINT `fk_flows_user`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
        ON DELETE RESTRICT,
    ADD CONSTRAINT `fk_flows_origin_flow`
        FOREIGN KEY (`origin_flow_id`) REFERENCES `flows` (`flow_id`)
        ON DELETE SET NULL,
    ADD CONSTRAINT `fk_flows_tutorial`
        FOREIGN KEY (`tutorial_id`) REFERENCES `tutorials` (`tutorial_id`)
        ON DELETE SET NULL;

ALTER TABLE `prompt_templates`
    ADD CONSTRAINT `fk_prompt_templates_block`
        FOREIGN KEY (`block_id`) REFERENCES `blocks` (`block_id`)
        ON DELETE SET NULL;

ALTER TABLE `tutorial_steps`
    ADD CONSTRAINT `fk_tutorial_steps_tutorial`
        FOREIGN KEY (`tutorial_id`) REFERENCES `tutorials` (`tutorial_id`)
        ON DELETE CASCADE;

ALTER TABLE `flow_blocks`
    ADD CONSTRAINT `fk_flow_blocks_flow`
        FOREIGN KEY (`flow_id`) REFERENCES `flows` (`flow_id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_flow_blocks_block`
        FOREIGN KEY (`block_id`) REFERENCES `blocks` (`block_id`)
        ON DELETE RESTRICT,
    ADD CONSTRAINT `fk_flow_blocks_prompt_template`
        FOREIGN KEY (`prompt_template_id`) REFERENCES `prompt_templates` (`prompt_template_id`)
        ON DELETE SET NULL;

ALTER TABLE `flow_categories`
    ADD CONSTRAINT `fk_flow_categories_flow`
        FOREIGN KEY (`flow_id`) REFERENCES `flows` (`flow_id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_flow_categories_category`
        FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`)
        ON DELETE CASCADE;

ALTER TABLE `flow_comments`
    ADD CONSTRAINT `fk_flow_comments_user`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_flow_comments_flow`
        FOREIGN KEY (`flow_id`) REFERENCES `flows` (`flow_id`)
        ON DELETE CASCADE;

ALTER TABLE `saved_tutorials`
    ADD CONSTRAINT `fk_saved_tutorials_user`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_saved_tutorials_tutorial`
        FOREIGN KEY (`tutorial_id`) REFERENCES `tutorials` (`tutorial_id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_saved_tutorials_flow`
        FOREIGN KEY (`flow_id`) REFERENCES `flows` (`flow_id`)
        ON DELETE SET NULL;

ALTER TABLE `flow_bookmarks`
    ADD CONSTRAINT `fk_flow_bookmarks_user`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_flow_bookmarks_flow`
        FOREIGN KEY (`flow_id`) REFERENCES `flows` (`flow_id`)
        ON DELETE CASCADE;

ALTER TABLE `tutorial_categories`
    ADD CONSTRAINT `fk_tutorial_categories_tutorial`
        FOREIGN KEY (`tutorial_id`) REFERENCES `tutorials` (`tutorial_id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_tutorial_categories_category`
        FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`)
        ON DELETE CASCADE;

ALTER TABLE `saved_flows`
    ADD CONSTRAINT `fk_saved_flows_user`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_saved_flows_flow`
        FOREIGN KEY (`flow_id`) REFERENCES `flows` (`flow_id`)
        ON DELETE CASCADE;

ALTER TABLE `flow_likes`
    ADD CONSTRAINT `fk_flow_likes_user`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_flow_likes_flow`
        FOREIGN KEY (`flow_id`) REFERENCES `flows` (`flow_id`)
        ON DELETE CASCADE;

ALTER TABLE `tutorial_blocks`
    ADD CONSTRAINT `fk_tutorial_blocks_block`
        FOREIGN KEY (`block_id`) REFERENCES `blocks` (`block_id`)
        ON DELETE RESTRICT,
    ADD CONSTRAINT `fk_tutorial_blocks_tutorial_step`
        FOREIGN KEY (`tutorial_step_id`) REFERENCES `tutorial_steps` (`tutorial_step_id`)
        ON DELETE CASCADE;

ALTER TABLE `ai_execution_logs`
    ADD CONSTRAINT `fk_ai_execution_logs_user`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
        ON DELETE SET NULL,
    ADD CONSTRAINT `fk_ai_execution_logs_flow`
        FOREIGN KEY (`flow_id`) REFERENCES `flows` (`flow_id`)
        ON DELETE SET NULL,
    ADD CONSTRAINT `fk_ai_execution_logs_flow_block`
        FOREIGN KEY (`flow_block_id`) REFERENCES `flow_blocks` (`flow_block_id`)
        ON DELETE SET NULL;

ALTER TABLE `flow_tags`
    ADD CONSTRAINT `fk_flow_tags_flow`
        FOREIGN KEY (`flow_id`) REFERENCES `flows` (`flow_id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `fk_flow_tags_tag`
        FOREIGN KEY (`tag_id`) REFERENCES `tags` (`tag_id`)
        ON DELETE CASCADE;
