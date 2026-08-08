package com.umc.learninglm.domain.flow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.umc.learninglm.domain.flow.repository.AiExecutionLogRepository;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
public class FlowPreviewService {

    private static final String PROVIDER_VERTEX_AI = "VERTEX_AI";
    private static final String REQUEST_TYPE_PREVIEW = "PREVIEW";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String RESULT_SOURCE_AI = "AI";
    private static final String RESULT_SOURCE_TEMPLATE = "TEMPLATE";

    private final BlockPromptCompiler blockPromptCompiler;
    private final AiHarnessCompiler aiHarnessCompiler;
    private final AiThinkingPolicy aiThinkingPolicy;
    private final AiModelClient aiModelClient;
    private final AiExecutionLogRepository aiExecutionLogRepository;
    private final ObjectMapper objectMapper;
    private final FlowAccessGuard flowAccessGuard;
    private final int defaultOutputTokens;

    public FlowPreviewService(
            BlockPromptCompiler blockPromptCompiler,
            AiHarnessCompiler aiHarnessCompiler,
            AiThinkingPolicy aiThinkingPolicy,
            AiModelClient aiModelClient,
            AiExecutionLogRepository aiExecutionLogRepository,
            ObjectMapper objectMapper,
            FlowAccessGuard flowAccessGuard,
            @Value("${ai.generation.default-output-tokens:${AI_DEFAULT_OUTPUT_TOKENS:4096}}")
            int defaultOutputTokens
    ) {
        this.blockPromptCompiler = blockPromptCompiler;
        this.aiHarnessCompiler = aiHarnessCompiler;
        this.aiThinkingPolicy = aiThinkingPolicy;
        this.aiModelClient = aiModelClient;
        this.aiExecutionLogRepository = aiExecutionLogRepository;
        this.objectMapper = objectMapper;
        this.flowAccessGuard = flowAccessGuard;
        this.defaultOutputTokens = defaultOutputTokens;
        if (defaultOutputTokens <= 0) {
            throw new IllegalArgumentException("AI 기본 출력 토큰은 1 이상이어야 합니다.");
        }
    }

    public FlowPreviewResponse previewFlow(Long flowId, FlowPreviewRequest request) {
        User user = flowAccessGuard.currentUser();
        Flow flow = flowAccessGuard.requireFlow(flowId);
        flowAccessGuard.requireOwner(flow, user);

        BlockPromptCompileResult compileResult = blockPromptCompiler.compile(
                toCompileRequests(request.blocks())
        );
        List<CompiledPromptFragment> fragments =
                compileResult.promptFragments();
        CompiledAiHarness harness = aiHarnessCompiler.compile(
                new HarnessCompileRequest(
                        resolveUserRequest(flow),
                        flow.getTitle(),
                        fragments,
                        compileResult.localActions(),
                        Map.of(),
                        defaultOutputTokens
                )
        );
        AiModelConfiguration configuration = aiThinkingPolicy.resolve(
                harness,
                fragments
        );

        try {
            AiGenerationResult result = aiModelClient.generate(
                    harness,
                    configuration
            );
            logSuccess(user, flow, harness, configuration, result);
            return new FlowPreviewResponse(
                    result.text(),
                    RESULT_SOURCE_AI,
                    result.model()
            );
        } catch (RestClientException | IllegalStateException exception) {
            log.warn(
                    "Vertex AI 호출 실패로 템플릿 결과를 반환합니다. flowId={}",
                    flowId,
                    exception
            );
            logFailure(user, flow, harness, configuration, exception);
            return new FlowPreviewResponse(
                    buildTemplateFallback(request.blocks()),
                    RESULT_SOURCE_TEMPLATE,
                    null
            );
        }
    }

    private List<BlockPromptCompileRequest> toCompileRequests(
            List<FlowBlockOptionRequest> blocks
    ) {
        return blocks.stream()
                .map(block -> new BlockPromptCompileRequest(
                        createNodeId(block),
                        block.blockId(),
                        block.blockOrder(),
                        null,
                        block.input() == null ? Map.of() : block.input(),
                        block.options() == null ? Map.of() : block.options(),
                        block.resolvedContext() == null
                                ? Map.of()
                                : block.resolvedContext(),
                        List.of()
                ))
                .toList();
    }

    private String createNodeId(FlowBlockOptionRequest block) {
        return "flow-block-" + block.blockOrder() + "-" + block.blockId();
    }

    private String resolveUserRequest(Flow flow) {
        if (flow.getPurpose() != null && !flow.getPurpose().isBlank()) {
            return flow.getPurpose();
        }
        if (flow.getExampleInput() != null && !flow.getExampleInput().isBlank()) {
            return flow.getExampleInput();
        }
        return flow.getTitle();
    }

    private String buildTemplateFallback(List<FlowBlockOptionRequest> blocks) {
        return "AI 결과를 생성하지 못했습니다. 배치된 블록 "
                + blocks.size()
                + "개를 기준으로 한 기본 템플릿 결과입니다.";
    }

    private void logSuccess(
            User user,
            Flow flow,
            CompiledAiHarness harness,
            AiModelConfiguration configuration,
            AiGenerationResult result
    ) {
        Map<String, Object> outputSnapshot = new LinkedHashMap<>();
        outputSnapshot.put("resultText", result.text());
        outputSnapshot.put("structuredOutput", result.structuredOutput());
        outputSnapshot.put("durationMs", result.durationMs());
        outputSnapshot.put("finishReason", result.finishReason());
        outputSnapshot.put("thoughtsTokenCount", result.thoughtsTokenCount());

        saveExecutionLog(
                user,
                flow,
                result.model(),
                STATUS_SUCCESS,
                inputSnapshot(harness, configuration),
                outputSnapshot,
                null,
                result.totalTokenCount()
        );
    }

    private void logFailure(
            User user,
            Flow flow,
            CompiledAiHarness harness,
            AiModelConfiguration configuration,
            RuntimeException exception
    ) {
        saveExecutionLog(
                user,
                flow,
                aiModelClient.modelName(),
                STATUS_FAILED,
                inputSnapshot(harness, configuration),
                null,
                exception.getMessage(),
                null
        );
    }

    private Map<String, Object> inputSnapshot(
            CompiledAiHarness harness,
            AiModelConfiguration configuration
    ) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("systemInstruction", harness.systemInstruction());
        snapshot.put("prompt", harness.prompt());
        snapshot.put("postActions", harness.postActions());
        snapshot.put("estimatedInputTokens", harness.estimatedInputTokens());
        snapshot.put("estimatedOutputTokens", harness.estimatedOutputTokens());
        snapshot.put("thinkingProfile", configuration.thinkingProfile().name());
        snapshot.put("thinkingBudget", configuration.thinkingBudget());
        snapshot.put("maxOutputTokens", configuration.maxOutputTokens());
        snapshot.put("temperature", configuration.temperature());
        return snapshot;
    }

    private void saveExecutionLog(
            User user,
            Flow flow,
            String modelName,
            String status,
            Map<String, Object> inputSnapshot,
            Map<String, Object> outputSnapshot,
            String errorMessage,
            Integer tokenUsage
    ) {
        AiExecutionLog executionLog = AiExecutionLog.create(
                user,
                flow,
                PROVIDER_VERTEX_AI,
                modelName,
                REQUEST_TYPE_PREVIEW,
                status,
                errorMessage,
                writeSnapshot(inputSnapshot),
                outputSnapshot == null ? null : writeSnapshot(outputSnapshot),
                tokenUsage
        );
        aiExecutionLogRepository.save(executionLog);
    }

    private String writeSnapshot(Map<String, Object> snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException exception) {
            throw new CustomException(ErrorCode.FLOW_OPTIONS_PARSE_FAILED);
        }
    }
}