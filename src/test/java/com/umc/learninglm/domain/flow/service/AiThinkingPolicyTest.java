package com.umc.learninglm.domain.flow.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.umc.learninglm.domain.flow.dto.ai.AiModelConfiguration;
import com.umc.learninglm.domain.flow.dto.harness.CompiledAiHarness;
import com.umc.learninglm.domain.flow.enums.ThinkingProfile;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AiThinkingPolicyTest {

    private final AiThinkingPolicy policy = new AiThinkingPolicy();

    @Test
    void selectsOffAndAppliesMinimumOutputLimitForSmallRequest() {
        CompiledAiHarness harness = harness(500, 100);

        AiModelConfiguration result = policy.resolve(harness, List.of());

        assertThat(result.thinkingProfile()).isEqualTo(ThinkingProfile.OFF);
        assertThat(result.thinkingBudget()).isZero();
        assertThat(result.maxOutputTokens()).isEqualTo(1024);
    }

    @Test
    void selectsBalancedAndAppliesMaximumOutputLimitForLargePrompt() {
        CompiledAiHarness harness = harness(4500, 10000);

        AiModelConfiguration result = policy.resolve(harness, List.of());

        assertThat(result.thinkingProfile()).isEqualTo(ThinkingProfile.BALANCED);
        assertThat(result.thinkingBudget()).isEqualTo(512);
        assertThat(result.maxOutputTokens()).isEqualTo(8192);
    }

    private CompiledAiHarness harness(
            int estimatedInputTokens,
            int estimatedOutputTokens
    ) {
        return new CompiledAiHarness(
                "system",
                "prompt",
                Map.of("type", "OBJECT"),
                List.of(),
                estimatedInputTokens,
                estimatedOutputTokens
        );
    }
}