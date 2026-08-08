package com.umc.learninglm.domain.flow.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.learninglm.domain.block.dto.prompt.CompiledLocalAction;
import com.umc.learninglm.domain.block.dto.prompt.CompiledPromptFragment;
import com.umc.learninglm.domain.block.dto.prompt.PromptArtifactValue;
import com.umc.learninglm.domain.block.dto.prompt.PromptFragment;
import com.umc.learninglm.domain.block.enums.BlockType;
import com.umc.learninglm.domain.block.enums.PromptExecutionType;
import com.umc.learninglm.domain.block.enums.PromptInputRole;
import com.umc.learninglm.domain.flow.dto.harness.CompiledAiHarness;
import com.umc.learninglm.domain.flow.dto.harness.HarnessCompileRequest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AiHarnessCompilerImplTest {

    private static final String GLOBAL_OUTPUT_POLICY =
            "별도 지정이 없으면 핵심 내용부터 간결하게 작성합니다.";

    private final AiHarnessCompiler compiler =
            new AiHarnessCompilerImpl(
                    new ObjectMapper(),
                    GLOBAL_OUTPUT_POLICY,
                    4096,
                    3
            );

    @Test
    void compilesFragmentsIntoOneOrderedHarness() {
        List<CompiledPromptFragment> fragments = List.of(
                fragment(
                        "output",
                        5L,
                        BlockType.OUTPUT,
                        5,
                        PromptExecutionType.AI_INSTRUCTION,
                        "마크다운 결과를 출력합니다.",
                        List.of()
                ),
                fragment(
                        "process",
                        3L,
                        BlockType.PROCESS,
                        3,
                        PromptExecutionType.AI_INSTRUCTION,
                        "코드의 문제점과 개선안을 분석합니다.",
                        List.of(new PromptArtifactValue(
                                PromptInputRole.EVIDENCE,
                                1,
                                "공식 권장사항",
                                "Spring 공식 문서"
                        ))
                ),
                fragment(
                        "input",
                        1L,
                        BlockType.INPUT,
                        1,
                        PromptExecutionType.CONFIG_ONLY,
                        "사용자 요청을 실행 기준으로 사용합니다.",
                        List.of(new PromptArtifactValue(
                                PromptInputRole.PRIMARY,
                                1,
                                "업로드 코드",
                                "class Sample {}"
                        ))
                ),
                fragment(
                        "review",
                        4L,
                        BlockType.REVIEW,
                        4,
                        PromptExecutionType.AI_INSTRUCTION,
                        "누락과 잘못된 근거를 다시 확인합니다.",
                        List.of()
                ),
                fragment(
                        "save",
                        6L,
                        BlockType.OUTPUT,
                        6,
                        PromptExecutionType.PERSISTENCE,
                        "결과를 저장합니다.",
                        List.of()
                )
        );

        CompiledAiHarness result = compiler.compile(
                new HarnessCompileRequest(
                        "업로드한 코드를 검토해 주세요.",
                        "코드 리팩토링",
                        fragments,
                        List.of(new CompiledLocalAction(
                                "save",
                                6L,
                                6,
                                PromptExecutionType.PERSISTENCE,
                                Map.of("title", "review result"),
                                Map.of("tags", List.of("review")),
                                Map.of(),
                                List.of()
                        )),
                        Map.of(),
                        4096
                )
        );

        assertThat(result.prompt())
                .contains("[USER REQUEST & TOPIC]")
                .contains("[PRIMARY INPUTS]")
                .contains("[EVIDENCE]")
                .contains("[ORDERED PROCESS INSTRUCTIONS]")
                .contains("[REVIEW CRITERIA]")
                .contains("[OUTPUT REQUIREMENTS]")
                .contains("[OUTPUT SCHEMA]")
                .contains("class Sample {}")
                .doesNotContain("결과를 저장합니다.");
        assertThat(result.prompt().indexOf("[ORDERED PROCESS INSTRUCTIONS]"))
                .isLessThan(result.prompt().indexOf("[REVIEW CRITERIA]"));
        assertThat(result.prompt().indexOf("[REVIEW CRITERIA]"))
                .isLessThan(result.prompt().indexOf("[OUTPUT REQUIREMENTS]"));
        assertThat(result.systemInstruction())
                .contains("[GLOBAL OUTPUT POLICY]")
                .contains(GLOBAL_OUTPUT_POLICY);
        assertThat(result.postActions())
                .singleElement()
                .satisfies(action -> {
                    assertThat(action.nodeId()).isEqualTo("save");
                    assertThat(action.executionType())
                            .isEqualTo(PromptExecutionType.PERSISTENCE);
                    assertThat(action.blockOrder()).isEqualTo(6);
                    assertThat(action.input())
                            .containsEntry("title", "review result");
                    assertThat(action.options()).containsKey("tags");
                });
        assertThat(result.responseSchema()).containsKey("properties");
        assertThat(result.estimatedInputTokens()).isPositive();
        assertThat(result.estimatedOutputTokens()).isEqualTo(4096);
    }

    private CompiledPromptFragment fragment(
            String nodeId,
            long blockId,
            BlockType stage,
            int blockOrder,
            PromptExecutionType executionType,
            String content,
            List<PromptArtifactValue> artifacts
    ) {
        return new CompiledPromptFragment(
                nodeId,
                executionType,
                new PromptFragment(
                        blockId,
                        blockId,
                        "v1",
                        stage,
                        blockOrder,
                        content
                ),
                artifacts
        );
    }
}