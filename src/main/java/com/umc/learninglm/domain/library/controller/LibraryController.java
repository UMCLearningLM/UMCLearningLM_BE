package com.umc.learninglm.domain.library.controller;

import com.umc.learninglm.domain.library.dto.response.LibraryDetailResponse;
import com.umc.learninglm.domain.library.dto.response.LibraryListResponse;
import com.umc.learninglm.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Library", description = "공개 활용 흐름 라이브러리 API")
@RestController
@RequestMapping("/api/library")
public class LibraryController {

    @GetMapping
    @Operation(
            summary = "공개 흐름 목록 조회",
            description = """
                    공개된 활용 흐름을 검색어, 카테고리, 난이도, 태그 및 정렬 조건에 따라 조회합니다.
                    조건에 맞는 전체 목록을 반환합니다.
                    """
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
                            LIBRARY40002: 지원하지 않는 정렬 기준입니다.
                            """
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "AUTH40104: 유효하지 않은 토큰입니다."
            )
    })
    @Parameter(
            name = "Authorization",
            description = "선택적 Bearer Access Token. 유효한 토큰이 있으면 좋아요 및 북마크 여부를 반영합니다.",
            in = ParameterIn.HEADER,
            required = false,
            example = "Bearer access-token"
    )
    public BaseResponse<LibraryListResponse> getLibraryFlows(
            @RequestHeader(value = "Authorization", required = false)
            String authorization,

            @Parameter(
                    description = "공개 흐름 제목, 설명, 작성자 또는 태그 검색어",
                    example = "리서치"
            )
            @RequestParam(required = false)
            String q,

            @Parameter(
                    description = "카테고리 코드",
                    example = "RESEARCH"
            )
            @RequestParam(required = false)
            String category,

            @Parameter(
                    description = "난이도 코드",
                    example = "BASIC"
            )
            @RequestParam(required = false)
            String difficulty,

            @Parameter(
                    description = "태그 식별자",
                    example = "1"
            )
            @RequestParam(required = false)
            Long tagId,

            @Parameter(
                    description = "정렬 기준. POPULAR 또는 LATEST",
                    example = "LATEST"
            )
            @RequestParam(defaultValue = "LATEST")
            String sort
    ) {
        return BaseResponse.success(
                new LibraryListResponse(
                        0,
                        List.of()
                )
        );
    }

    @GetMapping("/{flowId}")
    @Operation(
            summary = "공개 흐름 상세 조회",
            description = """
                    공개 흐름의 기본 정보, 블록 흐름, 예시 입력과 결과, 작성자 노트, 반응 정보 및 댓글을 조회합니다.
                    """
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
    @Parameter(
            name = "Authorization",
            description = "선택적 Bearer Access Token. 유효한 토큰이 있으면 좋아요 및 북마크 여부를 반영합니다.",
            in = ParameterIn.HEADER,
            required = false,
            example = "Bearer access-token"
    )
    public BaseResponse<LibraryDetailResponse> getLibraryFlowDetail(
            @PathVariable Long flowId,

            @RequestHeader(value = "Authorization", required = false)
            String authorization
    ) {
        return BaseResponse.success(null);
    }
}