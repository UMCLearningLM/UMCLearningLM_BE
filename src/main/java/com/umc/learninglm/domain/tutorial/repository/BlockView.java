package com.umc.learninglm.domain.tutorial.repository;

// blocks 테이블에서 튜토리얼이 필요한 컬럼만 읽는 읽기 전용 뷰 (엔티티 아님). blockType = 응답의 stage.
public record BlockView(Long blockId, String name, String blockType, String description) {
}
