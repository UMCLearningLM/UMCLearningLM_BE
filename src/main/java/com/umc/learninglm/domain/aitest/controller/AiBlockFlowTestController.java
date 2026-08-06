package com.umc.learninglm.domain.aitest.controller;

import com.umc.learninglm.domain.aitest.dto.request.AiBlockFlowTestRequest;
import com.umc.learninglm.domain.aitest.dto.response.AiBlockFlowTestResponse;
import com.umc.learninglm.domain.aitest.service.AiBlockFlowTestService;
import com.umc.learninglm.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI Test", description = "블록 프롬프트 조립 및 Vertex AI 임시 연동 테스트")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/temp/ai")
public class AiBlockFlowTestController {

    private final AiBlockFlowTestService aiBlockFlowTestService;

    @PostMapping("/block-refactoring")
    @Operation(
            summary = "블록 기반 코드 리팩토링 AI 테스트",
            description = """
                    실제 DB의 블록을 PromptFragment로 변환하고 역할별 AI Harness로 조립한 뒤
                    Thinking 자동 정책을 적용하여 Gemini를 한 번 호출하는 임시 테스트 API입니다.
                    """
    )
    public BaseResponse<AiBlockFlowTestResponse> execute(
            @Valid @RequestBody AiBlockFlowTestRequest request
    ) {
        return BaseResponse.success(aiBlockFlowTestService.execute(request));
    }
}