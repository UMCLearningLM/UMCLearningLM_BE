package com.umc.learninglm.domain.home.dto.query;

public record TutorialCategoryQuery(
        Long tutorialId,
        Long categoryId,
        String code,
        Integer sortOrder
) {
}