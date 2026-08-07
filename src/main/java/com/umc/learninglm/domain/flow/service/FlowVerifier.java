package com.umc.learninglm.domain.flow.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.learninglm.domain.block.entity.Block;
import com.umc.learninglm.domain.block.enums.BlockType;
import com.umc.learninglm.domain.block.repository.BlockRepository;
import com.umc.learninglm.domain.flow.dto.request.FlowBlockOptionRequest;
import com.umc.learninglm.domain.flow.dto.response.FlowVerifyRuleResultResponse;
import com.umc.learninglm.domain.flow.entity.Flow;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// WF-10 검증 패널 확정 규칙: verification_rules 테이블 없이 하드코딩으로 판정한다.
@Component
@RequiredArgsConstructor
public class FlowVerifier {

	public static final String STATUS_PASS = "PASS";
	public static final String STATUS_INSUFFICIENT = "INSUFFICIENT";
	public static final String STATUS_PENDING = "PENDING";

	private final BlockRepository blockRepository;
	private final ObjectMapper objectMapper;

	public List<FlowVerifyRuleResultResponse> verify(Flow flow, List<FlowBlockOptionRequest> blocks) {
		if (blocks.isEmpty()) {
			return pendingResultsForEmptyBlocks();
		}

		Map<Long, Block> blockById = blockLookup(blocks);

		FlowVerifyRuleResultResponse inputRule = coreBlockRule(1L, "입력 노드 CORE 블록", BlockType.INPUT, blocks, blockById);
		FlowVerifyRuleResultResponse processRule = coreBlockRule(2L, "프로세스 노드 CORE 블록", BlockType.PROCESS, blocks, blockById);
		FlowVerifyRuleResultResponse outputRule = coreBlockRule(3L, "결과 노드 CORE 블록", BlockType.OUTPUT, blocks, blockById);
		FlowVerifyRuleResultResponse requiredSlotRule = requiredSlotRule(blocks, blockById);

		boolean coreRulesPass = STATUS_PASS.equals(inputRule.status())
				&& STATUS_PASS.equals(processRule.status())
				&& STATUS_PASS.equals(outputRule.status())
				&& STATUS_PASS.equals(requiredSlotRule.status());
		FlowVerifyRuleResultResponse saveConditionRule = saveConditionRule(flow, coreRulesPass);

		List<FlowVerifyRuleResultResponse> results = new ArrayList<>();
		results.add(inputRule);
		results.add(processRule);
		results.add(outputRule);
		results.add(requiredSlotRule);
		results.add(saveConditionRule);
		return results;
	}

	private Map<Long, Block> blockLookup(List<FlowBlockOptionRequest> blocks) {
		List<Long> blockIds = blocks.stream().map(FlowBlockOptionRequest::blockId).distinct().toList();
		List<Block> found = blockRepository.findAllById(blockIds);
		if (found.size() != blockIds.size()) {
			throw new CustomException(ErrorCode.FLOW_BLOCK_NOT_FOUND);
		}
		return found.stream().collect(java.util.stream.Collectors.toMap(Block::getBlockId, b -> b));
	}

	private FlowVerifyRuleResultResponse coreBlockRule(
			Long ruleId,
			String name,
			BlockType targetType,
			List<FlowBlockOptionRequest> blocks,
			Map<Long, Block> blockById) {
		String stageLabel = targetType.name();
		boolean present = blocks.stream()
				.anyMatch(b -> blockById.get(b.blockId()).getBlockType() == targetType);

		String criteria = stageLabel + " 단계에 필수 블록 1개 이상이 포함되어야 합니다.";
		if (present) {
			return new FlowVerifyRuleResultResponse(
					ruleId, name, STATUS_PASS, criteria,
					stageLabel + " 단계에 블록이 포함되어 있습니다.", null, stageLabel);
		}
		return new FlowVerifyRuleResultResponse(
				ruleId, name, STATUS_INSUFFICIENT, criteria,
				stageLabel + " 단계에 블록이 없습니다.",
				stageLabel + " 단계에 블록을 1개 이상 추가하세요.", stageLabel);
	}

	private FlowVerifyRuleResultResponse requiredSlotRule(
			List<FlowBlockOptionRequest> blocks, Map<Long, Block> blockById) {
		String criteria = "각 블록의 필수 옵션이 모두 채워져야 합니다.";
		for (FlowBlockOptionRequest blockRequest : blocks) {
			Block block = blockById.get(blockRequest.blockId());
			List<String> requiredFields = requiredFields(block.getOptionSchema());
			Map<String, Object> options = blockRequest.options() == null ? Map.of() : blockRequest.options();

			for (String field : requiredFields) {
				Object value = options.get(field);
				if (isBlankValue(value)) {
					return new FlowVerifyRuleResultResponse(
							4L, "필수 슬롯 채움", STATUS_INSUFFICIENT, criteria,
							"\"" + block.getName() + "\" 노드의 \"" + field + "\" 값이 비어 있습니다.",
							block.getName() + " 노드의 " + field + "을(를) 채워주세요.",
							block.getBlockType().name());
				}
			}
		}
		return new FlowVerifyRuleResultResponse(
				4L, "필수 슬롯 채움", STATUS_PASS, criteria,
				"모든 블록의 필수 옵션이 채워져 있습니다.", null, null);
	}

	private FlowVerifyRuleResultResponse saveConditionRule(Flow flow, boolean coreRulesPass) {
		String criteria = "제목과 블록이 있어야 하며 위 검수 항목이 모두 통과해야 저장할 수 있습니다.";
		boolean hasTitle = flow.getTitle() != null && !flow.getTitle().isBlank();
		if (hasTitle && coreRulesPass) {
			return new FlowVerifyRuleResultResponse(
					5L, "저장 조건", STATUS_PASS, criteria, "저장 가능한 상태입니다.", null, null);
		}
		return new FlowVerifyRuleResultResponse(
				5L, "저장 조건", STATUS_PENDING, criteria,
				"아직 저장 조건을 모두 충족하지 못했습니다.",
				"제목과 필수 블록, 필수 옵션을 모두 채운 뒤 저장하세요.", null);
	}

	private List<FlowVerifyRuleResultResponse> pendingResultsForEmptyBlocks() {
		List<FlowVerifyRuleResultResponse> results = new ArrayList<>();
		results.add(new FlowVerifyRuleResultResponse(
				1L, "입력 노드 CORE 블록", STATUS_PENDING, "입력 단계에 필수 블록 1개 이상이 포함되어야 합니다.",
				"배치된 블록이 없습니다.", null, "INPUT"));
		results.add(new FlowVerifyRuleResultResponse(
				2L, "프로세스 노드 CORE 블록", STATUS_PENDING, "프로세스 단계에 필수 블록 1개 이상이 포함되어야 합니다.",
				"배치된 블록이 없습니다.", null, "PROCESS"));
		results.add(new FlowVerifyRuleResultResponse(
				3L, "결과 노드 CORE 블록", STATUS_PENDING, "결과 단계에 필수 블록 1개 이상이 포함되어야 합니다.",
				"배치된 블록이 없습니다.", null, "OUTPUT"));
		results.add(new FlowVerifyRuleResultResponse(
				4L, "필수 슬롯 채움", STATUS_PENDING, "각 블록의 필수 옵션이 모두 채워져야 합니다.",
				"배치된 블록이 없습니다.", null, null));
		results.add(new FlowVerifyRuleResultResponse(
				5L, "저장 조건", STATUS_PENDING, "제목과 블록이 있어야 하며 위 검수 항목이 모두 통과해야 저장할 수 있습니다.",
				"배치된 블록이 없습니다.", null, null));
		return results;
	}

	private List<String> requiredFields(String optionSchemaJson) {
		if (optionSchemaJson == null || optionSchemaJson.isBlank()) {
			return List.of();
		}
		try {
			JsonNode schema = objectMapper.readTree(optionSchemaJson);
			JsonNode required = schema.get("required");
			if (required == null || !required.isArray()) {
				return List.of();
			}
			return objectMapper.convertValue(required, new TypeReference<List<String>>() { });
		} catch (Exception e) {
			return List.of();
		}
	}

	private boolean isBlankValue(Object value) {
		if (value == null) {
			return true;
		}
		if (value instanceof String s) {
			return s.isBlank();
		}
		if (value instanceof List<?> list) {
			return list.isEmpty();
		}
		if (value instanceof Map<?, ?> map) {
			return map.isEmpty();
		}
		return false;
	}
}
