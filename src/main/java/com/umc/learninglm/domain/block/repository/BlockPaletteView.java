package com.umc.learninglm.domain.block.repository;

public record BlockPaletteView(
        Long blockId,
        String stage,
        String name,
        String description,
        String status,
        Boolean required
) {
}