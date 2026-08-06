package com.umc.learninglm.domain.aitest.service;

import com.umc.learninglm.domain.aitest.dto.request.AiBlockFlowTestRequest;
import com.umc.learninglm.domain.aitest.dto.response.AiBlockFlowTestResponse;
import com.umc.learninglm.domain.block.dto.prompt.BlockPromptCompileRequest;
import com.umc.learninglm.domain.block.dto.prompt.CompiledPromptFragment;
import com.umc.learninglm.domain.block.dto.prompt.PromptArtifactValue;
import com.umc.learninglm.domain.block.enums.PromptExecutionType;
import com.umc.learninglm.domain.block.enums.PromptInputRole;
import com.umc.learninglm.domain.block.service.BlockPromptCompiler;
import com.umc.learninglm.domain.flow.client.AiModelClient;
import com.umc.learninglm.domain.flow.dto.ai.AiGenerationResult;
import com.umc.learninglm.domain.flow.dto.ai.AiModelConfiguration;
import com.umc.learninglm.domain.flow.dto.harness.CompiledAiHarness;
import com.umc.learninglm.domain.flow.dto.harness.HarnessCompileRequest;
import com.umc.learninglm.domain.flow.service.AiHarnessCompiler;
import com.umc.learninglm.domain.flow.service.AiThinkingPolicy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiBlockFlowTestService {

    private static final List<String> BLOCK_KEYS = List.of(
            "IN-001",
            "CTX-005",
            "PR-008",
            "RV-007",
            "OUT-006"
    );

    private final JdbcTemplate jdbcTemplate;
    private final BlockPromptCompiler blockPromptCompiler;
    private final AiHarnessCompiler aiHarnessCompiler;
    private final AiThinkingPolicy aiThinkingPolicy;
    private final AiModelClient aiModelClient;

    public AiBlockFlowTestResponse execute(AiBlockFlowTestRequest request) {
        Map<String, Long> blockIds = findBlockIds();
        List<BlockPromptCompileRequest> compileRequests =
                createCompileRequests(request, blockIds);
        List<CompiledPromptFragment> fragments =
                blockPromptCompiler.compile(compileRequests);

        CompiledAiHarness harness = aiHarnessCompiler.compile(
                new HarnessCompileRequest(
                        request.request(),
                        "코드 리팩토링 및 개선 제안",
                        fragments,
                        Map.of(),
                        4096
                )
        );
        AiModelConfiguration modelConfiguration =
                aiThinkingPolicy.resolve(harness, fragments);
        AiGenerationResult aiResponse =
                aiModelClient.generate(harness, modelConfiguration);

        return new AiBlockFlowTestResponse(
                fragments,
                harness,
                modelConfiguration,
                aiResponse
        );
    }

    private Map<String, Long> findBlockIds() {
        String placeholders = String.join(
                ", ",
                BLOCK_KEYS.stream().map(key -> "?").toList()
        );
        String sql = "select block_id, block_key from blocks where block_key in ("
                + placeholders
                + ")";

        Map<String, Long> blockIds = jdbcTemplate.query(
                sql,
                preparedStatement -> {
                    for (int index = 0; index < BLOCK_KEYS.size(); index++) {
                        preparedStatement.setString(index + 1, BLOCK_KEYS.get(index));
                    }
                },
                resultSet -> {
                    Map<String, Long> results = new LinkedHashMap<>();
                    while (resultSet.next()) {
                        results.put(
                                resultSet.getString("block_key"),
                                resultSet.getLong("block_id")
                        );
                    }
                    return results;
                }
        );

        if (blockIds == null || blockIds.size() != BLOCK_KEYS.size()) {
            throw new IllegalStateException(
                    "AI 테스트에 필요한 블록 데이터가 모두 등록되어 있지 않습니다."
            );
        }
        return blockIds;
    }

    private List<BlockPromptCompileRequest> createCompileRequests(
            AiBlockFlowTestRequest request,
            Map<String, Long> blockIds
    ) {
        return List.of(
                new BlockPromptCompileRequest(
                        "input-request",
                        blockIds.get("IN-001"),
                        1,
                        PromptExecutionType.CONFIG_ONLY,
                        Map.of("request", request.request()),
                        Map.of("language", "KO"),
                        Map.of(),
                        List.of(artifact(
                                PromptInputRole.PRIMARY,
                                1,
                                "사용자 요청",
                                request.request()
                        ))
                ),
                new BlockPromptCompileRequest(
                        "context-role",
                        blockIds.get("CTX-005"),
                        6,
                        PromptExecutionType.CONFIG_ONLY,
                        Map.of(
                                "role",
                                "Spring Boot 시니어 백엔드 개발자이자 코드 리뷰어"
                        ),
                        Map.of("seniority", "SENIOR"),
                        Map.of("projectContext", request.projectContext()),
                        List.of(artifact(
                                PromptInputRole.REFERENCE,
                                1,
                                "프로젝트 배경",
                                request.projectContext()
                        ))
                ),
                new BlockPromptCompileRequest(
                        "process-review",
                        blockIds.get("PR-008"),
                        12,
                        PromptExecutionType.AI_INSTRUCTION,
                        Map.of(
                                "code", request.code(),
                                "context", request.projectContext()
                        ),
                        Map.of(
                                "categories",
                                List.of("BUG", "MAINTAINABILITY"),
                                "minSeverity",
                                "LOW"
                        ),
                        Map.of(
                                "officialGuidance",
                                request.officialGuidance()
                        ),
                        List.of(artifact(
                                PromptInputRole.EVIDENCE,
                                1,
                                "공식 권장사항",
                                request.officialGuidance()
                        ))
                ),
                new BlockPromptCompileRequest(
                        "review-tone",
                        blockIds.get("RV-007"),
                        17,
                        PromptExecutionType.AI_INSTRUCTION,
                        Map.of("document", "PROCESS 단계에서 작성한 코드 분석 결과"),
                        Map.of("tone", "SENIOR_REVIEWER"),
                        Map.of(),
                        List.of(artifact(
                                PromptInputRole.CONSTRAINT,
                                1,
                                "리뷰 톤",
                                "시니어 개발자 코드 리뷰 톤"
                        ))
                ),
                new BlockPromptCompileRequest(
                        "output-document",
                        blockIds.get("OUT-006"),
                        18,
                        PromptExecutionType.AI_INSTRUCTION,
                        Map.of("content", "앞선 단계의 전체 코드 리뷰 결과"),
                        Map.of("format", "MARKDOWN"),
                        Map.of(),
                        List.of()
                )
        );
    }

    private PromptArtifactValue artifact(
            PromptInputRole role,
            int order,
            String label,
            Object value
    ) {
        return new PromptArtifactValue(role, order, label, value);
    }
}