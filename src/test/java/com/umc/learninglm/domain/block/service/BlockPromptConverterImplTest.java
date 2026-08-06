package com.umc.learninglm.domain.block.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.learninglm.domain.block.dto.prompt.PromptFragment;
import com.umc.learninglm.domain.block.dto.prompt.PromptFragmentRequest;
import com.umc.learninglm.domain.block.entity.Block;
import com.umc.learninglm.domain.block.entity.PromptTemplate;
import com.umc.learninglm.domain.block.enums.BlockType;
import com.umc.learninglm.domain.block.repository.BlockPromptRepository;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BlockPromptConverterImplTest {

	private BlockPromptRepository blockPromptRepository;
	private BlockPromptConverter blockPromptConverter;

	@BeforeEach
	void setUp() {
		blockPromptRepository = mock(BlockPromptRepository.class);
		blockPromptConverter = new BlockPromptConverterImpl(
				blockPromptRepository,
				new ObjectMapper()
		);
	}

	@Test
	void convertsAssignedTemplateToPromptFragment() {
		Block block = createBlock(
				10L,
				BlockType.PROCESS,
				5L,
				BlockType.PROCESS,
				"v1",
				"제공된 자료를 날짜별로 요약합니다.",
				"{blockInstruction} {input.topic} 자료를 {detailLevel} 수준으로 "
						+ "요약하고 핵심 항목을 {maxItems}개 작성합니다. "
						+ "자료: {resolvedContext.sourceData}"
		);
		when(blockPromptRepository.findWithActivePromptTemplateByBlockId(10L))
				.thenReturn(Optional.of(block));

		PromptFragment result = blockPromptConverter.convert(
				new PromptFragmentRequest(
						10L,
						2,
						Map.of("topic", "최근 서울 평균 온도"),
						Map.of(
								"detailLevel", "자세한",
								"maxItems", 7
						),
						Map.of("sourceData", "기상청 관측 자료")
				)
		);

		assertThat(result.blockId()).isEqualTo(10L);
		assertThat(result.promptTemplateId()).isEqualTo(5L);
		assertThat(result.templateVersion()).isEqualTo("v1");
		assertThat(result.stage()).isEqualTo(BlockType.PROCESS);
		assertThat(result.blockOrder()).isEqualTo(2);
		assertThat(result.content()).isEqualTo(
				"제공된 자료를 날짜별로 요약합니다. "
						+ "최근 서울 평균 온도 자료를 자세한 수준으로 "
						+ "요약하고 핵심 항목을 7개 작성합니다. "
						+ "자료: 기상청 관측 자료"
		);
		verify(blockPromptRepository)
				.findWithActivePromptTemplateByBlockId(10L);
	}

	@Test
	void serializesRootVariablesAsJson() {
		Block block = createBlock(
				10L,
				BlockType.CONTEXT,
				5L,
				BlockType.CONTEXT,
				"v1",
				"컨텍스트를 구성합니다.",
				"입력: {input}\n옵션: {options}\n컨텍스트: {resolvedContext}"
		);
		when(blockPromptRepository.findWithActivePromptTemplateByBlockId(10L))
				.thenReturn(Optional.of(block));

		PromptFragment result = blockPromptConverter.convert(
				new PromptFragmentRequest(
						10L,
						1,
						Map.of("topic", "생성형 AI"),
						Map.of("maxItems", 7),
						Map.of("sources", List.of("기상청", "통계청"))
				)
		);

		assertThat(result.content()).isEqualTo(
				"입력: {\"topic\":\"생성형 AI\"}\n"
						+ "옵션: {\"maxItems\":7}\n"
						+ "컨텍스트: {\"sources\":[\"기상청\",\"통계청\"]}"
		);
	}

	@Test
	void throwsExceptionWhenTemplateVariableIsMissing() {
		Block block = createBlock(
				10L,
				BlockType.PROCESS,
				5L,
				BlockType.PROCESS,
				"v1",
				"요약합니다.",
				"핵심 항목을 {maxItems}개 작성합니다."
		);
		when(blockPromptRepository.findWithActivePromptTemplateByBlockId(10L))
				.thenReturn(Optional.of(block));

		assertErrorCode(
				new PromptFragmentRequest(
						10L, 1, Map.of(), Map.of(), Map.of()
				),
				ErrorCode.BLOCK_PROMPT_VARIABLE_MISSING
		);
	}

	@Test
	void throwsExceptionWhenShortVariableExistsInMultipleAreas() {
		Block block = createBlock(
				10L,
				BlockType.PROCESS,
				5L,
				BlockType.PROCESS,
				"v1",
				"요약합니다.",
				"주제: {topic}"
		);
		when(blockPromptRepository.findWithActivePromptTemplateByBlockId(10L))
				.thenReturn(Optional.of(block));

		assertErrorCode(
				new PromptFragmentRequest(
						10L,
						1,
						Map.of("topic", "입력 주제"),
						Map.of("topic", "옵션 주제"),
						Map.of()
				),
				ErrorCode.BLOCK_PROMPT_VARIABLE_AMBIGUOUS
		);
	}

	@Test
	void throwsExceptionWhenActiveTemplateDoesNotExist() {
		when(blockPromptRepository.findWithActivePromptTemplateByBlockId(10L))
				.thenReturn(Optional.empty());

		assertErrorCode(
				new PromptFragmentRequest(
						10L, 1, Map.of(), Map.of(), Map.of()
				),
				ErrorCode.BLOCK_PROMPT_TEMPLATE_NOT_FOUND
		);
	}

	@Test
	void throwsExceptionWhenBlockAndTemplateStagesAreDifferent() {
		Block block = createBlock(
				10L,
				BlockType.PROCESS,
				5L,
				BlockType.REVIEW,
				"v1",
				"요약합니다.",
				"{blockInstruction}"
		);
		when(blockPromptRepository.findWithActivePromptTemplateByBlockId(10L))
				.thenReturn(Optional.of(block));

		assertErrorCode(
				new PromptFragmentRequest(
						10L, 1, Map.of(), Map.of(), Map.of()
				),
				ErrorCode.BLOCK_PROMPT_TEMPLATE_STAGE_MISMATCH
		);
	}

	private void assertErrorCode(
			PromptFragmentRequest request,
			ErrorCode expectedErrorCode
	) {
		assertThatThrownBy(() -> blockPromptConverter.convert(request))
				.isInstanceOf(CustomException.class)
				.extracting(exception -> ((CustomException) exception).getErrorCode())
				.isEqualTo(expectedErrorCode);
	}

	private Block createBlock(
			Long blockId,
			BlockType blockType,
			Long promptTemplateId,
			BlockType templateStage,
			String version,
			String promptInstruction,
			String promptBody
	) {
		PromptTemplate promptTemplate = mock(PromptTemplate.class);
		when(promptTemplate.getPromptTemplateId()).thenReturn(promptTemplateId);
		when(promptTemplate.getStage()).thenReturn(templateStage);
		when(promptTemplate.getVersion()).thenReturn(version);
		when(promptTemplate.getPromptBody()).thenReturn(promptBody);

		Block block = mock(Block.class);
		when(block.getBlockId()).thenReturn(blockId);
		when(block.getBlockType()).thenReturn(blockType);
		when(block.getPromptInstruction()).thenReturn(promptInstruction);
		when(block.getPromptTemplate()).thenReturn(promptTemplate);

		return block;
	}
}
