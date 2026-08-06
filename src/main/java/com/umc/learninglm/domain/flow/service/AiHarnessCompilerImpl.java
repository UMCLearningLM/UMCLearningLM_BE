package com.umc.learninglm.domain.flow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiHarnessCompilerImpl implements AiHarnessCompiler {

	private static final String SYSTEM_INSTRUCTION = """
            당신은 LearningLM Workflow 실행 엔진입니다.
            각 섹션과 블록 지시를 순서대로 반영하고 최종 결과는 지정된 JSON Schema에 맞춰 한 번만 반환합니다.
            제공되지 않은 근거나 사실을 임의로 생성하지 않습니다.
            """;
	private static final int DEFAULT_OUTPUT_TOKENS = 2048;
	private static final int APPROXIMATE_CHARACTERS_PER_TOKEN = 3;

	private final ObjectMapper objectMapper;

	@Override
	public CompiledAiHarness compile(HarnessCompileRequest request) {
		List<CompiledPromptFragment> fragments = sortedFragments(request.fragments());
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
				? DEFAULT_OUTPUT_TOKENS
				: request.estimatedOutputTokens();

		return new CompiledAiHarness(
				SYSTEM_INSTRUCTION,
				prompt.toString().trim(),
				responseSchema,
				postActions(fragments),
				estimateTokens(SYSTEM_INSTRUCTION + prompt),
				estimatedOutputTokens
		);
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
			List<CompiledPromptFragment> fragments
	) {
		return fragments.stream()
				.filter(fragment -> fragment.executionType()
						== PromptExecutionType.LOCAL_TRANSFORM
						|| fragment.executionType()
						== PromptExecutionType.PERSISTENCE)
				.map(fragment -> new LocalPostAction(
						fragment.nodeId(),
						fragment.fragment().blockId(),
						fragment.executionType()
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
				(text.length() + APPROXIMATE_CHARACTERS_PER_TOKEN - 1)
						/ APPROXIMATE_CHARACTERS_PER_TOKEN
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
