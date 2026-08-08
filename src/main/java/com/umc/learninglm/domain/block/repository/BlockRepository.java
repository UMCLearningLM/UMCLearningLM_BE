package com.umc.learninglm.domain.block.repository;

import com.umc.learninglm.domain.block.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockRepository extends JpaRepository<Block, Long> {
}
