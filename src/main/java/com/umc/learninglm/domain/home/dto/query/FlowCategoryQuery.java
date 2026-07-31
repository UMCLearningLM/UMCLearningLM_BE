package com.umc.learninglm.domain.home.dto.query;

public record FlowCategoryQuery(
        Long flowId,
        Long categoryId,
        String code,
        Integer sortOrder
) {
}
