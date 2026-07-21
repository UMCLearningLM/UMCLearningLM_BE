package com.umc.learninglm.domain.home.controller;

import com.umc.learninglm.domain.home.dto.response.HomeResponse;
import com.umc.learninglm.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home", description = "홈 화면 API")
@RestController
@RequestMapping("/api/home")
public class HomeController {

    @GetMapping
    @Operation(
            summary = "홈 화면 통합 조회",
            description = "홈 화면에 필요한 카테고리, 추천 튜토리얼, 인기 공개 흐름과 사용자별 학습 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "홈 화면 조회 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "AUTH40104: 유효하지 않은 토큰입니다."
            )
    })
    @Parameter(
            name = "Authorization",
            description = "선택적 Bearer Access Token. 유효한 토큰이 있으면 개인화 정보를 반환합니다.",
            in = ParameterIn.HEADER,
            required = false,
            example = "Bearer access-token"
    )
    public BaseResponse<HomeResponse> getHome(
            @RequestHeader(value = "Authorization", required = false)
            String authorization
    ) {
        return BaseResponse.success(null);
    }
}