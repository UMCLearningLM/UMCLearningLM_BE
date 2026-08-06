package com.umc.learninglm.domain.flow.service;

import com.umc.learninglm.domain.flow.dto.harness.CompiledAiHarness;
import com.umc.learninglm.domain.flow.dto.harness.HarnessCompileRequest;

public interface AiHarnessCompiler {

	CompiledAiHarness compile(HarnessCompileRequest request);
}
