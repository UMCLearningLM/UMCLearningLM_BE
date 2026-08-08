package com.umc.learninglm.domain.flow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.learninglm.domain.block.dto.prompt.CompiledLocalAction;
import com.umc.learninglm.domain.block.dto.prompt.CompiledPromptFragment;
import com.umc.learninglm.domain.block.dto.prompt.PromptArtifactValue;
import com.umc.learninglm.domain.block.enums.BlockType;
import com.umc.learninglm.domain.block.enums.PromptExecutionType;
import com.umc.learninglm.domain.block.enums.PromptInputRole;
import com.umc.learninglm.domain.flow.dto.harness.CompiledAiHarness;
import com.umc.learninglm.domain.flow.dto.harness.HarnessCompileRequest;
import com.umc.learninglm.domain.flow.dto.harness.LocalPostAction;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiHarnessCompilerImpl implements AiHarnessCompiler {

	private static final String SYSTEM_INSTRUCTION = """
            당신은 LearningLM Workflow 실행 엔진입니다.
            각 섹션과 블록 지시를 순서대로 반영하고 최종 결과는 지정된 JSON Schema에 맞춰 한 번만 반환합니다.
            제공되지 않은 근거나 사실을 임의로 생성하지 않습니다.
            """;
	private static final String DEFAULT_GLOBAL_OUTPUT_POLICY = """
			사용자가 지정한 출력 형식, 분량, 항목 수를 최우선으로 따릅니다.
			별도 지정이 없으면 핵심 내용만 간결하게 작성하고 중복 설명과 불필요한 서론을 생략합니다.
			출력이 길어질 경우 결론과 주요 근거를 먼저 작성합니다.
			""";

	private final ObjectMapper objectMapper;
	private final String globalOutputPolicy;
	private final int defaultOutputTokens;
	private final int approximateCharactersPerToken;

	public AiHarnessCompilerImpl(
			ObjectMapper objectMapper,
			@Value("${ai.prompt.global-output-policy:${AI_GLOBAL_OUTPUT_POLICY:}}")
			String globalOutputPolicy,
			@Value("${ai.generation.default-output-tokens:${AI_DEFAULT_OUTPUT_TOKENS:4096}}")
			int defaultOutputTokens,
			@Value("${ai.generation.token-estimate-characters-per-token:${AI_TOKEN_ESTIMATE_CHARACTERS_PER_TOKEN:3}}")
			int approximateCharactersPerToken
	) {
		this.objectMapper = objectMapper;
		this.globalOutputPolicy = globalOutputPolicy == null
				|| globalOutputPolicy.isBlank()
				? DEFAULT_GLOBAL_OUTPUT_POLICY
				: globalOutputPolicy.trim();
		this.defaultOutputTokens = defaultOutputTokens;
		this.approximateCharactersPerToken = approximateCharactersPerToken;
		if (defaultOutputTokens <= 0 || approximateCharactersPerToken <= 0) {
			throw new IllegalArgumentException("AI 토큰 환경변수 설정이 올바르지 않습니다.");
		}
	}

	@Override
	public CompiledAiHarness compile(HarnessCompileRequest request) {
		List<CompiledPromptFragment> fragments = sortedFragments(request.fragments());
		List<CompiledLocalAction> localActions =
				sortedLocalActions(request.localActions());
		List<CompiledPromptFragment> modelFragments = fragments.stream()
				.filter(this::isModelInput)
				.toList();
		Map<String, Object> responseSchema = hasSchema(request.responseSchema())
				? request.responseSchema()
				: defaultResponseSchema();

		StringBuilder prompt = new StringBuilder();
		appendSection(
				prompt,
				"USER REQUEST & TOPIC",
				userRequestSection(request, modelFragments)
		);
		appendSection(
				prompt,
				"PRIMARY INPUTS",
				artifactSection(fragments, PromptInputRole.PRIMARY)
		);
		appendSection(
				prompt,
				"REFERENCES",
				artifactSection(fragments, PromptInputRole.REFERENCE)
		);
		appendSection(
				prompt,
				"CONSTRAINTS",
				joinNotBlank(
						artifactSection(fragments, PromptInputRole.CONSTRAINT),
						stageSection(modelFragments, BlockType.CONTEXT)
				)
		);
		appendSection(
				prompt,
				"EVIDENCE",
				artifactSection(fragments, PromptInputRole.EVIDENCE)
		);
		appendSection(
				prompt,
				"ORDERED PROCESS INSTRUCTIONS",
				stageSection(modelFragments, BlockType.PROCESS)
		);
		appendSection(
				prompt,
				"REVIEW CRITERIA",
				stageSection(modelFragments, BlockType.REVIEW)
		);
		appendSection(
				prompt,
				"OUTPUT REQUIREMENTS",
				stageSection(modelFragments, BlockType.OUTPUT)
		);
		appendSection(
				prompt,
				"OUTPUT SCHEMA",
				toJson(responseSchema)
		);

		int estimatedOutputTokens = request.estimatedOutputTokens() == null
				|| request.estimatedOutputTokens() <= 0
				? defaultOutputTokens
				: request.estimatedOutputTokens();

		return new CompiledAiHarness(
				buildSystemInstruction(),
				prompt.toString().trim(),
				responseSchema,
				postActions(localActions),
				estimateTokens(buildSystemInstruction() + prompt),
				estimatedOutputTokens
		);
	}

	private String buildSystemInstruction() {
		return SYSTEM_INSTRUCTION.trim()
				+ "\n\n[GLOBAL OUTPUT POLICY]\n"
				+ globalOutputPolicy;
	}

	private List<CompiledPromptFragment> sortedFragments(
			List<CompiledPromptFragment> fragments
	) {
		if (fragments == null) {
			return List.of();
		}

		return fragments.stream()
				.sorted(Comparator
						.comparingInt((CompiledPromptFragment fragment) ->
						fragment.fragment().stage().ordinal())
						.thenComparingInt(fragment -> fragment.fragment().blockOrder()))
				.toList();
	}

	private List<CompiledLocalAction> sortedLocalActions(
			List<CompiledLocalAction> localActions
	) {
		if (localActions == null) {
			return List.of();
		}

		return localActions.stream()
				.sorted(Comparator.comparingInt(action ->
						action.blockOrder() == null
								? Integer.MAX_VALUE
								: action.blockOrder()))
				.toList();
	}

	private boolean isModelInput(CompiledPromptFragment fragment) {
		return fragment.executionType() != PromptExecutionType.LOCAL_TRANSFORM
				&& fragment.executionType() != PromptExecutionType.PERSISTENCE;
	}

	private String userRequestSection(
			HarnessCompileRequest request,
			List<CompiledPromptFragment> fragments
	) {
		List<String> values = new ArrayList<>();
		if (request.userRequest() != null && !request.userRequest().isBlank()) {
			values.add("사용자 요청:\n" + request.userRequest());
		}
		if (request.topic() != null && !request.topic().isBlank()) {
			values.add("주제:\n" + request.topic());
		}
		String inputInstructions = stageSection(fragments, BlockType.INPUT);
		if (!inputInstructions.isBlank()) {
			values.add(inputInstructions);
		}
		return String.join("\n\n", values);
	}

	private String artifactSection(
			List<CompiledPromptFragment> fragments,
			PromptInputRole role
	) {
		return fragments.stream()
				.flatMap(fragment -> fragment.artifacts().stream())
				.filter(artifact -> artifact.role() == role)
				.sorted(Comparator.comparingInt(this::artifactOrder))
				.map(this::formatArtifact)
				.collect(Collectors.joining("\n"));
	}

	private int artifactOrder(PromptArtifactValue artifact) {
		return artifact.order() == null ? Integer.MAX_VALUE : artifact.order();
	}

	private String formatArtifact(PromptArtifactValue artifact) {
		String label = artifact.label() == null || artifact.label().isBlank()
				? artifact.role().name()
				: artifact.label();
		return "- " + label + ": " + toPromptText(artifact.value());
	}

	private String stageSection(
			List<CompiledPromptFragment> fragments,
			BlockType stage
	) {
		return fragments.stream()
				.filter(fragment -> fragment.fragment().stage() == stage)
				.map(fragment -> "[" + fragment.nodeId() + "]\n"
						+ fragment.fragment().content())
				.collect(Collectors.joining("\n\n"));
	}

	private List<LocalPostAction> postActions(
			List<CompiledLocalAction> localActions
	) {
		return localActions.stream()
				.map(action -> new LocalPostAction(
						action.nodeId(),
						action.blockId(),
						action.blockOrder(),
						action.executionType(),
						action.input(),
						action.options(),
						action.resolvedContext(),
						action.artifacts()
				))
				.toList();
	}

	private void appendSection(
			StringBuilder prompt,
			String title,
			String content
	) {
		if (content == null || content.isBlank()) {
			return;
		}

		if (!prompt.isEmpty()) {
			prompt.append("\n\n");
		}
		prompt.append('[').append(title).append("]\n").append(content.trim());
	}

	private String joinNotBlank(String first, String second) {
		return java.util.stream.Stream.of(first, second)
				.filter(value -> value != null && !value.isBlank())
				.collect(Collectors.joining("\n\n"));
	}

	private int estimateTokens(CharSequence text) {
		return Math.max(
				1,
				(text.length() + approximateCharactersPerToken - 1)
						/ approximateCharactersPerToken
		);
	}

	private boolean hasSchema(Map<String, Object> schema) {
		return schema != null && !schema.isEmpty();
	}

	private Map<String, Object> defaultResponseSchema() {
		Map<String, Object> outputItem = new LinkedHashMap<>();
		outputItem.put("type", "OBJECT");
		outputItem.put("properties", Map.of(
				"key", Map.of("type", "STRING"),
				"format", Map.of("type", "STRING"),
				"content", Map.of("type", "STRING")
		));
		outputItem.put("required", List.of("key", "format", "content"));

		Map<String, Object> schema = new LinkedHashMap<>();
		schema.put("type", "OBJECT");
		schema.put("properties", Map.of(
				"outputs", Map.of(
						"type", "ARRAY",
						"items", outputItem
				)
		));
		schema.put("required", List.of("outputs"));
		return schema;
	}

	private String toPromptText(Object value) {
		if (value == null) {
			return "null";
		}
		if (value instanceof String
				|| value instanceof Number
				|| value instanceof Boolean) {
			return String.valueOf(value);
		}
		return toJson(value);
	}

	private String toJson(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		} catch (JsonProcessingException exception) {
			throw new IllegalStateException("AI Harness JSON 직렬화에 실패했습니다.", exception);
		}
	}
}
