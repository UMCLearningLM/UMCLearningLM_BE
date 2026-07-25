package com.umc.learninglm.domain.block.controller;

import com.umc.learninglm.domain.block.dto.response.BlockListResponse;
import com.umc.learninglm.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Block", description = "스튜디오 블록 팔레트 API")
@RestController
@RequestMapping("/api/blocks")
public class BlockController {

    @GetMapping
    @Operation(
            summary = "블록 팔레트 목록 조회",
            description = """
                    스튜디오 좌측 블록 팔레트에 노출할 블록 목록을 단계별로 그룹화하여 조회합니다.

                    tutorialId가 없으면 자유 제작 모드의 블록 목록을 반환하고,
                    tutorialId가 있으면 해당 공식 튜토리얼의 블록 설정과 필수 여부를 반영합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "블록 팔레트 목록 조회 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "BLOCK40001: 유효하지 않은 stage 값입니다."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "TUTORIAL40401: 존재하지 않는 튜토리얼입니다."
            )
    })
    public BaseResponse<BlockListResponse> getBlocks(
            @Parameter(
                    description = "블록 이름 또는 설명 검색어. 부분 일치",
                    example = "요약"
            )
            @RequestParam(required = false)
            String q,

            @Parameter(
                    description = "조회할 블록 단계",
                    example = "PROCESS"
            )
            @RequestParam(required = false)
            String stage,

            @Parameter(
                    description = "공식 튜토리얼 식별자. 없으면 자유 제작 모드로 조회",
                    example = "1"
            )
            @RequestParam(required = false)
            Long tutorialId
    ) {
        return BaseResponse.success(
                new BlockListResponse(
                        tutorialId == null ? "CREATE" : "TUTORIAL",
                        tutorialId,
                        List.of()
                )
        );
    }
}
