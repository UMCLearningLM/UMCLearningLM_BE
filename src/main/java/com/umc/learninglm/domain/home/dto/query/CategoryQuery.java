package com.umc.learninglm.domain.home.dto.query;

public record CategoryQuery(
        Long categoryId,
        String code,
        Integer sortOrder
) {
}
