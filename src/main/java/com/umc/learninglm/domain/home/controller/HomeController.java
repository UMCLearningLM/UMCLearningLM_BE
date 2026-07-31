package com.umc.learninglm.domain.home.controller;

import com.umc.learninglm.domain.home.dto.response.HomeResponse;
import com.umc.learninglm.domain.home.service.HomeService;
import com.umc.learninglm.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home", description = "홈 화면 API")
@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping
    @Operation(
            summary = "홈 화면 통합 조회",
            description = """
                    홈 화면에 필요한 추천 튜토리얼, 목적별 카테고리,
                    인기 공개 흐름을 조회합니다.

                    Access Token은 선택 사항입니다.
                    유효한 Access Token이 있으면 이어서 학습하기와
                    최근 저장 항목을 추가로 반환합니다.
                    """,
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            }
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
    public BaseResponse<HomeResponse> getHome() {
        return BaseResponse.success(homeService.getHome());
    }
}