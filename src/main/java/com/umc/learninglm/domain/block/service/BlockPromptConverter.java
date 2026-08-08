package com.umc.learninglm.domain.block.service;

import com.umc.learninglm.domain.block.dto.prompt.PromptFragment;
import com.umc.learninglm.domain.block.dto.prompt.PromptFragmentRequest;

public interface BlockPromptConverter {

	PromptFragment convert(PromptFragmentRequest request);
}
