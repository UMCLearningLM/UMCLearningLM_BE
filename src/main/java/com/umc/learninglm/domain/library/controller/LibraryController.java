package com.umc.learninglm.domain.library.controller;

import com.umc.learninglm.domain.library.dto.response.LibraryDetailResponse;
import com.umc.learninglm.domain.library.dto.response.LibraryListResponse;
import com.umc.learninglm.domain.library.service.LibraryService;
import com.umc.learninglm.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Library", description = "공개 활용 흐름 라이브러리 API")
@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    @GetMapping
    @Operation(
            summary = "공개 흐름 목록 조회",
            description = """
                    공개된 활용 흐름을 검색어, 카테고리, 난이도 조건에 따라 조회합니다.
                    조건에 맞는 전체 목록을 반환합니다.
                    유효한 토큰이 있으면 좋아요 여부를 반영합니다.
                    """,
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "공개 흐름 목록 조회 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            LIBRARY40001: 잘못된 필터 값입니다.
                            LIBRARY40003: 카테고리는 최대 3개까지 선택할 수 있습니다.
                            LIBRARY40004: 난이도는 최대 2개까지 선택할 수 있습니다.
                            """
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "AUTH40104: 유효하지 않은 토큰입니다."
            )
    })
    public BaseResponse<LibraryListResponse> getLibraryFlows(
            @Parameter(
                    description = "공개 흐름 제목, 요약 또는 태그명 검색어"
            )
            @RequestParam(required = false)
            String q,

            @Parameter(
                    description = "카테고리 식별자 목록. 최대 3개"
            )
            @RequestParam(required = false)
            List<Long> categoryIds,

            @Parameter(
                    description = "난이도 목록. BEGINNER / BASIC / ADVANCED 중 최대 2개"
            )
            @RequestParam(required = false)
            List<String> difficulties
    ) {
        return BaseResponse.success(
                libraryService.getLibraryFlows(
                        q,
                        categoryIds,
                        difficulties
                )
        );
    }

    @GetMapping("/{flowId}")
    @Operation(
            summary = "공개 흐름 상세 조회",
            description = """
                공개 흐름의 기본 정보, 블록 흐름, 예시 입력과 결과,
                작성자 노트 및 활성 댓글을 조회합니다.
                유효한 토큰이 있으면 좋아요 및 북마크 여부를 반영합니다.
                """,
            security = {
                @SecurityRequirement(name = "bearerAuth")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "공개 흐름 상세 조회 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "AUTH40104: 유효하지 않은 토큰입니다."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "LIBRARY40401: 공개 흐름을 찾을 수 없습니다."
            )
    })
    public BaseResponse<LibraryDetailResponse> getLibraryFlowDetail(
            @PathVariable Long flowId
    ) {
        return BaseResponse.success(
                libraryService.getLibraryFlowDetail(flowId)
        );
    }
}