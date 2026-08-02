package com.umc.learninglm.domain.block.service;

import com.umc.learninglm.domain.block.dto.response.BlockListResponse;
import com.umc.learninglm.domain.block.dto.response.BlockListResponse.BlockResponse;
import com.umc.learninglm.domain.block.dto.response.BlockListResponse.StageResponse;
import com.umc.learninglm.domain.block.enums.BlockStage;
import com.umc.learninglm.domain.block.repository.BlockPaletteRepository;
import com.umc.learninglm.domain.block.repository.BlockPaletteView;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlockService {

    // 자유 제작 모드
    private static final String CREATE_MODE = "CREATE";

    // 공식 튜토리얼 모드
    private static final String TUTORIAL_MODE = "TUTORIAL";

    private final BlockPaletteRepository blockPaletteRepository;

    public BlockListResponse getBlocks(
            String q,
            String stage,
            Long tutorialId
    ) {
        // 검색어의 앞뒤 공백 제거
        String keyword = normalizeKeyword(q);

        // stage 값을 enum으로 검증 및 변환
        BlockStage blockStage = parseStage(stage);

        // tutorialId가 있으면 공개 튜토리얼인지 확인
        if (tutorialId != null) {
            validateTutorial(tutorialId);
        }

        // tutorialId 유무에 따라 조회 모드 구분
        List<BlockPaletteView> blockViews =
                tutorialId == null
                        ? blockPaletteRepository.findCreateModeBlocks(
                        keyword,
                        toStageValue(blockStage)
                )
                        : blockPaletteRepository.findTutorialModeBlocks(
                        tutorialId,
                        keyword,
                        toStageValue(blockStage)
                );

        // 조회된 블록을 stage별로 그룹화
        List<StageResponse> stages = groupByStage(blockViews);

        return new BlockListResponse(
                tutorialId == null ? CREATE_MODE : TUTORIAL_MODE,
                tutorialId,
                stages
        );
    }

    private void validateTutorial(Long tutorialId) {
        // 존재하지 않거나 미공개 튜토리얼이면 404 반환
        if (!blockPaletteRepository.existsPublishedTutorial(tutorialId)) {
            throw new CustomException(ErrorCode.TUTORIAL_NOT_FOUND);
        }
    }

    private BlockStage parseStage(String stage) {
        // stage 미입력 시 전체 단계 조회
        if (stage == null) {
            return null;
        }

        String normalizedStage = stage.trim();

        if (normalizedStage.isEmpty()) {
            throw new CustomException(ErrorCode.BLOCK_INVALID_STAGE);
        }

        try {
            return BlockStage.valueOf(
                    normalizedStage.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new CustomException(ErrorCode.BLOCK_INVALID_STAGE);
        }
    }

    private String normalizeKeyword(String q) {
        // 검색어 미입력 시 검색 조건을 적용하지 않음
        if (q == null) {
            return null;
        }

        String normalizedKeyword = q.trim();

        return normalizedKeyword.isEmpty()
                ? null
                : normalizedKeyword;
    }

    private String toStageValue(BlockStage blockStage) {
        return blockStage == null
                ? null
                : blockStage.name();
    }

    private List<StageResponse> groupByStage(
            List<BlockPaletteView> blockViews
    ) {
        Map<BlockStage, List<BlockResponse>> groupedBlocks =
                new EnumMap<>(BlockStage.class);

        for (BlockPaletteView view : blockViews) {
            BlockStage stage;

            try {
                stage = BlockStage.valueOf(view.stage());
            } catch (IllegalArgumentException exception) {
                throw new CustomException(
                        ErrorCode.BLOCK_STAGE_CONFIGURATION_INVALID
                );
            }

            groupedBlocks
                    .computeIfAbsent(
                            stage,
                            ignored -> new ArrayList<>()
                    )
                    .add(toBlockResponse(view));
        }

        // enum 선언 순서대로 stage 응답 생성
        return Arrays.stream(BlockStage.values())
                .filter(groupedBlocks::containsKey)
                .map(stage -> new StageResponse(
                        stage.name(),
                        stage.getLabel(),
                        List.copyOf(groupedBlocks.get(stage))
                ))
                .toList();
    }

    private BlockResponse toBlockResponse(BlockPaletteView view) {
        return new BlockResponse(
                view.blockId(),
                view.name(),
                view.description(),
                view.status(),
                view.required()
        );
    }
}