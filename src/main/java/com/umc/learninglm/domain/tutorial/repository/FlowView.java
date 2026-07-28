package com.umc.learninglm.domain.tutorial.repository;

// flows 테이블에서 튜토리얼이 필요한 컬럼만 읽는 읽기 전용 뷰 (엔티티 아님)
public record FlowView(String flowType, String exampleInput, String exampleResult, Long tutorialId) {
}
