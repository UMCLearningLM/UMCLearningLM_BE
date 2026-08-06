package com.umc.learninglm.domain.block.service;

import com.umc.learninglm.domain.block.dto.prompt.BlockPromptCompileRequest;
import com.umc.learninglm.domain.block.dto.prompt.CompiledPromptFragment;
import java.util.List;

public interface BlockPromptCompiler {

	List<CompiledPromptFragment> compile(
			List<BlockPromptCompileRequest> requests
	);
}
