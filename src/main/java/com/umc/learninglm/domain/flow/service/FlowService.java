package com.umc.learninglm.domain.flow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.learninglm.domain.auth.entity.User;
import com.umc.learninglm.domain.block.entity.Block;
import com.umc.learninglm.domain.block.repository.BlockRepository;
import com.umc.learninglm.domain.category.entity.Category;
import com.umc.learninglm.domain.category.enums.CategoryCode;
import com.umc.learninglm.domain.category.repository.CategoryRepository;
import com.umc.learninglm.domain.flow.dto.request.FlowBlockRequest;
import com.umc.learninglm.domain.flow.dto.request.FlowCreateRequest;
import com.umc.learninglm.domain.flow.dto.request.FlowUpdateRequest;
import com.umc.learninglm.domain.flow.dto.request.FlowVerifyRequest;
import com.umc.learninglm.domain.flow.dto.response.FlowBlockResponse;
import com.umc.learninglm.domain.flow.dto.response.FlowCategoryResponse;
import com.umc.learninglm.domain.flow.dto.response.FlowCreateResponse;
import com.umc.learninglm.domain.flow.dto.response.FlowDeleteResponse;
import com.umc.learninglm.domain.flow.dto.response.FlowDetailResponse;
import com.umc.learninglm.domain.flow.dto.response.FlowUpdateResponse;
import com.umc.learninglm.domain.flow.dto.response.FlowVerifyResponse;
import com.umc.learninglm.domain.flow.dto.response.FlowVerifyRuleResultResponse;
import com.umc.learninglm.domain.flow.dto.response.FlowVerifySummaryResponse;
import com.umc.learninglm.domain.flow.entity.Flow;
import com.umc.learninglm.domain.flow.entity.FlowBlock;
import com.umc.learninglm.domain.flow.entity.FlowCategory;
import com.umc.learninglm.domain.flow.entity.PromptTemplate;
import com.umc.learninglm.domain.flow.enums.FlowDifficulty;
import com.umc.learninglm.domain.flow.enums.FlowMode;
import com.umc.learninglm.domain.flow.enums.FlowStatus;
import com.umc.learninglm.domain.flow.enums.FlowVisibility;
import com.umc.learninglm.domain.flow.repository.FlowBlockRepository;
import com.umc.learninglm.domain.flow.repository.FlowBookmarkRepository;
import com.umc.learninglm.domain.flow.repository.FlowCategoryRepository;
import com.umc.learninglm.domain.flow.repository.FlowCommentRepository;
import com.umc.learninglm.domain.flow.repository.FlowLikeRepository;
import com.umc.learninglm.domain.flow.repository.FlowRepository;
import com.umc.learninglm.domain.flow.repository.FlowTagRepository;
import com.umc.learninglm.domain.flow.repository.PromptTemplateRepository;
import com.umc.learninglm.domain.tutorial.entity.Tutorial;
import com.umc.learninglm.domain.tutorial.repository.TutorialRepository;
import com.umc.learninglm.global.error.CustomException;
import com.umc.learninglm.global.error.ErrorCode;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlowService {

	private final FlowRepository flowRepository;
	private final FlowBlockRepository flowBlockRepository;
	private final FlowCategoryRepository flowCategoryRepository;
	private final FlowLikeRepository flowLikeRepository;
	private final FlowCommentRepository flowCommentRepository;
	private final FlowBookmarkRepository flowBookmarkRepository;
	private final FlowTagRepository flowTagRepository;
	private final BlockRepository blockRepository;
	private final PromptTemplateRepository promptTemplateRepository;
	private final CategoryRepository categoryRepository;
	private final TutorialRepository tutorialRepository;
	private final ObjectMapper objectMapper;
	private final FlowVerifier flowVerifier;
	private final FlowAccessGuard flowAccessGuard;

	@Transactional
	public FlowCreateResponse createFlow(FlowCreateRequest request) {
		User user = flowAccessGuard.currentUser();
		FlowMode mode = parseEnum(FlowMode.class, request.mode());

		Tutorial tutorial = request.tutorialId() == null
				? null
				: tutorialRepository.findById(request.tutorialId())
						.orElseThrow(() -> new CustomException(ErrorCode.TUTORIAL_NOT_FOUND));

		Flow originFlow = request.originFlowId() == null
				? null
				: flowRepository.findById(request.originFlowId())
						.orElseThrow(() -> new CustomException(ErrorCode.FLOW_ORIGIN_NOT_FOUND));

		Flow flow = Flow.create(user, tutorial, originFlow, mode);
		flow = flowRepository.saveAndFlush(flow);

		if (originFlow != null) {
			copyBlocks(originFlow, flow);
		}

		return new FlowCreateResponse(
				flow.getFlowId(),
				flow.getTitle(),
				flow.getMode().name(),
				flow.getStatus().name(),
				flow.getCreatedAt());
	}

	public FlowDetailResponse getFlow(Long flowId) {
		User user = flowAccessGuard.currentUser();
		Flow flow = flowAccessGuard.requireFlow(flowId);
		if (flow.getVisibility() != FlowVisibility.PUBLIC) {
			flowAccessGuard.requireOwner(flow, user);
		}

		List<FlowCategoryResponse> categories = flowCategoryRepository.findByFlow_FlowId(flowId).stream()
				.map(this::toCategoryResponse)
				.toList();

		List<FlowBlockResponse> blockFlow = flowBlockRepository.findByFlow_FlowIdOrderByBlockOrderAsc(flowId).stream()
				.map(this::toBlockResponse)
				.toList();

		return new FlowDetailResponse(
				flow.getFlowId(),
				flow.getTitle(),
				flow.getSummary(),
				flow.getPurpose(),
				flow.getDifficulty() == null ? null : flow.getDifficulty().name(),
				categories,
				flow.getMode().name(),
				flow.getFlowType().name(),
				flow.getVisibility().name(),
				flow.getStatus().name(),
				flow.getAuthorNote(),
				flow.getExampleInput(),
				flow.getExampleResult(),
				flow.getOriginFlow() == null ? null : flow.getOriginFlow().getFlowId(),
				blockFlow,
				flow.getCreatedAt(),
				flow.getUpdatedAt());
	}

	@Transactional
	public FlowUpdateResponse updateFlow(Long flowId, FlowUpdateRequest request) {
		User user = flowAccessGuard.currentUser();
		Flow flow = flowAccessGuard.requireFlow(flowId);
		flowAccessGuard.requireOwner(flow, user);

		FlowDifficulty difficulty = request.difficulty() == null
				? flow.getDifficulty()
				: parseEnum(FlowDifficulty.class, request.difficulty());
		FlowVisibility visibility = parseEnum(FlowVisibility.class, request.visibility());
		FlowStatus status = parseEnum(FlowStatus.class, request.status());

		flow.updateDetails(
				request.title(),
				request.summary(),
				request.purpose(),
				difficulty,
				visibility,
				status,
				request.authorNote(),
				request.exampleInput(),
				request.exampleResult());

		replaceCategories(flow, request.categoryIds());
		replaceBlocks(flow, request.blocks());

		flow = flowRepository.saveAndFlush(flow);

		return new FlowUpdateResponse(flow.getFlowId(), flow.getStatus().name(), flow.getUpdatedAt());
	}

	@Transactional
	public FlowDeleteResponse deleteFlow(Long flowId) {
		User user = flowAccessGuard.currentUser();
		Flow flow = flowAccessGuard.requireFlow(flowId);
		flowAccessGuard.requireOwner(flow, user);

		flowLikeRepository.deleteByFlow_FlowId(flowId);
		flowCommentRepository.deleteByFlow_FlowId(flowId);
		flowBookmarkRepository.deleteByFlow_FlowId(flowId);
		flowTagRepository.deleteByFlow_FlowId(flowId);
		flowCategoryRepository.deleteByFlow_FlowId(flowId);
		flowBlockRepository.deleteByFlow_FlowId(flowId);
		flowRepository.delete(flow);

		return new FlowDeleteResponse(flowId, true);
	}

	public FlowVerifyResponse verifyFlow(Long flowId, FlowVerifyRequest request) {
		User user = flowAccessGuard.currentUser();
		Flow flow = flowAccessGuard.requireFlow(flowId);
		flowAccessGuard.requireOwner(flow, user);

		List<FlowVerifyRuleResultResponse> results = flowVerifier.verify(flow, request.blocks());

		FlowVerifySummaryResponse summary = summarize(results);
		boolean allPass = results.stream().allMatch(r -> "PASS".equals(r.status()));
		String totalStatus = allPass ? "PASS" : "FAIL";

		return new FlowVerifyResponse(totalStatus, summary, results);
	}

	private void copyBlocks(Flow originFlow, Flow flow) {
		List<FlowBlock> originBlocks = flowBlockRepository.findByFlow_FlowIdOrderByBlockOrderAsc(originFlow.getFlowId());
		for (FlowBlock originBlock : originBlocks) {
			flowBlockRepository.save(FlowBlock.create(
					flow,
					originBlock.getBlock(),
					originBlock.getPromptTemplate(),
					originBlock.getBlockOrder(),
					originBlock.getOptions()));
		}
	}

	private void replaceCategories(Flow flow, List<Long> categoryIds) {
		flowCategoryRepository.deleteByFlow_FlowId(flow.getFlowId());
		List<Category> categories = categoryRepository.findAllById(categoryIds);
		if (categories.size() != categoryIds.size()) {
			throw new CustomException(ErrorCode.FLOW_INVALID_REQUEST);
		}
		for (Category category : categories) {
			flowCategoryRepository.save(FlowCategory.create(flow, category));
		}
	}

	private void replaceBlocks(Flow flow, List<FlowBlockRequest> blockRequests) {
		flowBlockRepository.deleteByFlow_FlowId(flow.getFlowId());
		for (FlowBlockRequest blockRequest : blockRequests) {
			Block block = blockRepository.findById(blockRequest.blockId())
					.orElseThrow(() -> new CustomException(ErrorCode.FLOW_BLOCK_NOT_FOUND));
			PromptTemplate promptTemplate = blockRequest.promptTemplateId() == null
					? null
					: promptTemplateRepository.findById(blockRequest.promptTemplateId()).orElse(null);
			String options = writeOptions(blockRequest.options());

			flowBlockRepository.save(FlowBlock.create(
					flow, block, promptTemplate, blockRequest.blockOrder(), options));
		}
	}

	private FlowCategoryResponse toCategoryResponse(FlowCategory flowCategory) {
		Category category = flowCategory.getCategory();
		CategoryCode code = parseEnum(CategoryCode.class, category.getName());
		return new FlowCategoryResponse(category.getCategoryId(), code.name(), code.getDisplayName());
	}

	private FlowBlockResponse toBlockResponse(FlowBlock flowBlock) {
		Block block = flowBlock.getBlock();
		return new FlowBlockResponse(
				flowBlock.getFlowBlockId(),
				block.getBlockId(),
				block.getName(),
				block.getBlockType().name(),
				flowBlock.getBlockOrder(),
				readOptions(flowBlock.getOptions()),
				flowBlock.getInputValue(),
				flowBlock.getOutputValue(),
				flowBlock.getExecutionMode().name(),
				flowBlock.getPromptTemplate() == null ? null : flowBlock.getPromptTemplate().getPromptTemplateId());
	}

	private String writeOptions(Map<String, Object> options) {
		try {
			return objectMapper.writeValueAsString(options == null ? Map.of() : options);
		} catch (JsonProcessingException e) {
			throw new CustomException(ErrorCode.FLOW_OPTIONS_PARSE_FAILED);
		}
	}

	private Map<String, Object> readOptions(String options) {
		if (options == null || options.isBlank()) {
			return Map.of();
		}
		try {
			return objectMapper.readValue(options, new TypeReference<Map<String, Object>>() { });
		} catch (Exception e) {
			throw new CustomException(ErrorCode.FLOW_OPTIONS_PARSE_FAILED);
		}
	}

	private FlowVerifySummaryResponse summarize(List<FlowVerifyRuleResultResponse> results) {
		int pass = 0;
		int insufficient = 0;
		int pending = 0;
		for (FlowVerifyRuleResultResponse result : results) {
			switch (result.status()) {
				case "PASS" -> pass++;
				case "INSUFFICIENT" -> insufficient++;
				case "PENDING" -> pending++;
				default -> { }
			}
		}
		return new FlowVerifySummaryResponse(pass, insufficient, pending);
	}

	private <E extends Enum<E>> E parseEnum(Class<E> enumType, String value) {
		try {
			return Enum.valueOf(enumType, value);
		} catch (IllegalArgumentException | NullPointerException e) {
			throw new CustomException(ErrorCode.FLOW_INVALID_REQUEST);
		}
	}
}
