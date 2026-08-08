package com.umc.learninglm.domain.flow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.learninglm.domain.auth.entity.User;
import com.umc.learninglm.domain.block.dto.prompt.BlockPromptCompileRequest;
import com.umc.learninglm.domain.block.dto.prompt.BlockPromptCompileResult;
import com.umc.learninglm.domain.block.dto.prompt.CompiledPromptFragment;
import com.umc.learninglm.domain.block.service.BlockPromptCompiler;
import com.umc.learninglm.domain.flow.client.AiModelClient;
import com.umc.learninglm.domain.flow.dto.ai.AiGenerationResult;
import com.umc.learninglm.domain.flow.dto.ai.AiModelConfiguration;
import com.umc.learninglm.domain.flow.dto.harness.CompiledAiHarness;
import com.umc.learninglm.domain.flow.dto.harness.HarnessCompileRequest;
import com.umc.learninglm.domain.flow.dto.request.FlowBlockOptionRequest;
import com.umc.learninglm.domain.flow.dto.request.FlowPreviewRequest;
import com.umc.learninglm.domain.flow.dto.response.FlowPreviewResponse;
import com.umc.learninglm.domain.flow.entity.AiExecutionLog;
import com.umc.learninglm.domain.flow.entity.Flow;
import com.umc.learninglm.domain.flow.enums.ThinkingProfile;
import com.umc.learninglm.domain.flow.repository.AiExecutionLogRepository;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class FlowPreviewServiceTest {

    private BlockPromptCompiler blockPromptCompiler;
    private AiHarnessCompiler aiHarnessCompiler;
    private AiThinkingPolicy aiThinkingPolicy;
    private AiModelClient aiModelClient;
    private AiExecutionLogRepository aiExecutionLogRepository;
    private FlowAccessGuard flowAccessGuard;
    private FlowPreviewService flowPreviewService;

    @BeforeEach
    void setUp() {
        blockPromptCompiler = mock(BlockPromptCompiler.class);
        aiHarnessCompiler = mock(AiHarnessCompiler.class);
        aiThinkingPolicy = mock(AiThinkingPolicy.class);
        aiModelClient = mock(AiModelClient.class);
        aiExecutionLogRepository = mock(AiExecutionLogRepository.class);
        flowAccessGuard = mock(FlowAccessGuard.class);
        flowPreviewService = new FlowPreviewService(
                blockPromptCompiler,
                aiHarnessCompiler,
                aiThinkingPolicy,
                aiModelClient,
                aiExecutionLogRepository,
                new ObjectMapper(),
                flowAccessGuard,
                4096
        );
    }

    @Test
    void compilesFlowBlocksAndCallsConfiguredAiModelOnce() throws Exception {
        User user = mock(User.class);
        Flow flow = mock(Flow.class);
        when(flow.getTitle()).thenReturn("코드 리뷰");
        when(flow.getPurpose()).thenReturn("업로드한 코드를 개선해 주세요.");
        when(flowAccessGuard.currentUser()).thenReturn(user);
        when(flowAccessGuard.requireFlow(1L)).thenReturn(flow);

        List<CompiledPromptFragment> fragments = List.of();
        BlockPromptCompileResult compileResult =
                new BlockPromptCompileResult(fragments, List.of());
        CompiledAiHarness harness = new CompiledAiHarness(
                "system",
                "compiled prompt",
                Map.of("type", "OBJECT"),
                List.of(),
                120,
                4096
        );
        AiModelConfiguration configuration = new AiModelConfiguration(
                ThinkingProfile.LOW,
                256,
                4096,
                0.2
        );
        AiGenerationResult generationResult = new AiGenerationResult(
                "gemini-2.5-flash",
                "generated result",
                new ObjectMapper().readTree("{\"outputs\":[]}"),
                120,
                30,
                5,
                155,
                800,
                "STOP",
                ThinkingProfile.LOW
        );

        when(blockPromptCompiler.compile(any())).thenReturn(compileResult);
        when(aiHarnessCompiler.compile(any(HarnessCompileRequest.class)))
                .thenReturn(harness);
        when(aiThinkingPolicy.resolve(harness, fragments))
                .thenReturn(configuration);
        when(aiModelClient.generate(harness, configuration))
                .thenReturn(generationResult);

        FlowPreviewResponse response = flowPreviewService.previewFlow(
                1L,
                new FlowPreviewRequest(List.of(
                        new FlowBlockOptionRequest(
                                10L,
                                1,
                                Map.of("request", "코드를 분석해 주세요."),
                                Map.of("detailLevel", "DETAILED"),
                                Map.of("sourceData", "parsed source")
                        )
                ))
        );

        assertThat(response.resultText()).isEqualTo("generated result");
        assertThat(response.resultSource()).isEqualTo("AI");
        assertThat(response.modelName()).isEqualTo("gemini-2.5-flash");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BlockPromptCompileRequest>> compileCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(blockPromptCompiler).compile(compileCaptor.capture());
        BlockPromptCompileRequest compileRequest = compileCaptor.getValue().get(0);
        assertThat(compileRequest.nodeId()).isEqualTo("flow-block-1-10");
        assertThat(compileRequest.executionType()).isNull();
        assertThat(compileRequest.input())
                .containsEntry("request", "코드를 분석해 주세요.");
        assertThat(compileRequest.options())
                .containsEntry("detailLevel", "DETAILED");
        assertThat(compileRequest.resolvedContext())
                .containsEntry("sourceData", "parsed source");

        ArgumentCaptor<AiExecutionLog> logCaptor =
                ArgumentCaptor.forClass(AiExecutionLog.class);
        verify(aiExecutionLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getProvider()).isEqualTo("VERTEX_AI");
        assertThat(logCaptor.getValue().getTokenUsage()).isEqualTo(155);
    }
}