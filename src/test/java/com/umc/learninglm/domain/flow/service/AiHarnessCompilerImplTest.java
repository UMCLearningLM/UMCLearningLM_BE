package com.umc.learninglm.domain.flow.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final AiHarnessCompiler compiler =
            new AiHarnessCompilerImpl(new ObjectMapper());

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
        assertThat(result.postActions())
                .singleElement()
                .satisfies(action -> {
                    assertThat(action.nodeId()).isEqualTo("save");
                    assertThat(action.executionType())
                            .isEqualTo(PromptExecutionType.PERSISTENCE);
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