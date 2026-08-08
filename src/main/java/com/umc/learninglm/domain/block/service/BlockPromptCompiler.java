package com.umc.learninglm.domain.block.service;

import com.umc.learninglm.domain.block.dto.prompt.BlockPromptCompileRequest;
import com.umc.learninglm.domain.block.dto.prompt.BlockPromptCompileResult;
import java.util.List;

public interface BlockPromptCompiler {

	BlockPromptCompileResult compile(
			List<BlockPromptCompileRequest> requests
	);
}
