package com.umc.learninglm.domain.block.enums;

public enum BlockType {

    INPUT("입력"),
    CONTEXT("컨텍스트"),
    PROCESS("프로세스"),
    REVIEW("검토"),
    OUTPUT("결과");

    private final String label;

    BlockType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}