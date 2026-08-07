package com.umc.learninglm.domain.flow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.learninglm.domain.auth.entity.User;
import com.umc.learninglm.domain.block.entity.Block;
import com.umc.learninglm.domain.block.repository.BlockRepository;
import com.umc.learninglm.domain.flow.client.GeminiClient;
import com.umc.learninglm.domain.flow.dto.request.FlowBlockOptionRequest;
import com.umc.learninglm.domain.flow.dto.request.FlowPreviewRequest;
import com.umc.learninglm.domain.flow.dto.response.FlowPreviewResponse;
import com.umc.learninglm.domain.flow.entity.AiExecutionLog;
import com.umc.learninglm.domain.flow.entity.Flow;
import com.umc.learninglm.domain.flow.repository.AiExecutionLogRepository;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowPreviewService {

	private static final String PROVIDER_GEMINI = "GEMINI";
	private static final String REQUEST_TYPE_PREVIEW = "PREVIEW";
	private static final String STATUS_SUCCESS = "SUCCESS";
	private static final String STATUS_FAILED = "FAILED";
	private static final String RESULT_SOURCE_AI = "AI";
	private static final String RESULT_SOURCE_TEMPLATE = "TEMPLATE";

	private final BlockRepository blockRepository;
	private final AiExecutionLogRepository aiExecutionLogRepository;
	private final GeminiClient geminiClient;
	private final ObjectMapper objectMapper;
	private final FlowAccessGuard flowAccessGuard;

	// Gemini 호출을 DB 트랜잭션 밖에서 동기 실행하기 위해 메서드 레벨 @Transactional을 두지 않는다.
	// currentUser/requireFlow 조회와 logExecution의 save는 각 Repository 메서드 자체가 트랜잭션을 관리한다.
	public FlowPreviewResponse previewFlow(Long flowId, FlowPreviewRequest request) {
		User user = flowAccessGuard.currentUser();
		Flow flow = flowAccessGuard.requireFlow(flowId);
		flowAccessGuard.requireOwner(flow, user);

		String prompt = buildPrompt(request.blocks());

		try {
			String resultText = geminiClient.generateContent(prompt);
			logExecution(user, flow, geminiClient.getModel(), STATUS_SUCCESS, prompt, resultText, null);
			return new FlowPreviewResponse(resultText, RESULT_SOURCE_AI, geminiClient.getModel());
		} catch (RestClientException | IllegalStateException e) {
			log.warn("Gemini 호출 실패, TEMPLATE Fallback으로 전환합니다. flowId={}", flowId, e);
			logExecution(user, flow, geminiClient.getModel(), STATUS_FAILED, prompt, null, e.getMessage());
			String fallbackText = buildTemplateFallback(request.blocks());
			return new FlowPreviewResponse(fallbackText, RESULT_SOURCE_TEMPLATE, null);
		}
	}

	// TODO: 프롬프트 조합 규칙 팀 확정 대기 중 — 확정되면 이 메서드 내부만 교체.
	// 임시 버전: 블록 순서대로 "블록명: options"를 줄바꿈으로 나열.
	private String buildPrompt(List<FlowBlockOptionRequest> blocks) {
		Map<Long, Block> blockById = blockRepository.findAllById(
						blocks.stream().map(FlowBlockOptionRequest::blockId).distinct().toList())
				.stream()
				.collect(java.util.stream.Collectors.toMap(Block::getBlockId, b -> b));

		StringBuilder sb = new StringBuilder();
		for (FlowBlockOptionRequest blockRequest : blocks) {
			Block block = blockById.get(blockRequest.blockId());
			String name = block == null ? String.valueOf(blockRequest.blockId()) : block.getName();
			sb.append(name).append(": ").append(blockRequest.options()).append('\n');
		}
		return sb.toString();
	}

	private String buildTemplateFallback(List<FlowBlockOptionRequest> blocks) {
		return "예시 결과를 생성하지 못했습니다. 배치된 블록 " + blocks.size() + "개를 기준으로 한 기본 템플릿 결과입니다.";
	}

	private void logExecution(
			User user, Flow flow, String modelName, String status, String prompt, String resultText, String errorMessage) {
		AiExecutionLog executionLog = AiExecutionLog.create(
				user,
				flow,
				PROVIDER_GEMINI,
				modelName,
				REQUEST_TYPE_PREVIEW,
				status,
				errorMessage,
				writeSnapshot(Map.of("prompt", prompt)),
				resultText == null ? null : writeSnapshot(Map.of("resultText", resultText)),
				null);
		aiExecutionLogRepository.save(executionLog);
	}

	private String writeSnapshot(Map<String, Object> snapshot) {
		try {
			return objectMapper.writeValueAsString(snapshot);
		} catch (JsonProcessingException e) {
			throw new CustomException(ErrorCode.FLOW_OPTIONS_PARSE_FAILED);
		}
	}
}
