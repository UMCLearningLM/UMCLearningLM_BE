package com.umc.learninglm.domain.aitest.dto.response;

import com.umc.learninglm.domain.block.dto.prompt.CompiledPromptFragment;
import com.umc.learninglm.domain.flow.dto.ai.AiGenerationResult;
import com.umc.learninglm.domain.flow.dto.ai.AiModelConfiguration;
import com.umc.learninglm.domain.flow.dto.harness.CompiledAiHarness;
import java.util.List;

public record AiBlockFlowTestResponse(
        List<CompiledPromptFragment> fragments,
        CompiledAiHarness harness,
        AiModelConfiguration modelConfiguration,
        AiGenerationResult aiResponse
) {
}