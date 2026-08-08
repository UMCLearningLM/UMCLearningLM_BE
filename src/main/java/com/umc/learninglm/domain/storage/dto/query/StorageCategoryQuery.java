package com.umc.learninglm.domain.storage.dto.query;

// ownerId는 튜토리얼 조회 시 tutorialId, 흐름 조회 시 flowId
// code는 categories.name에 저장된 코드값(RESEARCH 등)
public record StorageCategoryQuery(
        Long ownerId,
        Long categoryId,
        String code
) {
}
