package com.combine.auditorium.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.combine.auditorium.entity.AIConfig;
import com.combine.auditorium.mapper.AIConfigMapper;
import com.combine.auditorium.service.AIConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AIConfigServiceImpl implements AIConfigService {

    private final AIConfigMapper aiConfigMapper;

    @Override
    public AIConfig getConfig() {
        AIConfig config = aiConfigMapper.selectOne(new LambdaQueryWrapper<AIConfig>().last("limit 1"));
        if (config == null) {
            config = new AIConfig();
            config.setTemperature(0.7);
            config.setTopK(3);
            config.setKnowledgeLimit(3);
            config.setUpdatedAt(LocalDateTime.now());
            aiConfigMapper.insert(config);
        }
        return config;
    }

    @Override
    @Transactional
    public AIConfig updateConfig(AIConfig config) {
        AIConfig current = getConfig();
        if (config.getTemperature() != null) {
            current.setTemperature(config.getTemperature());
        }
        if (config.getTopK() != null) {
            current.setTopK(config.getTopK());
        }
        if (config.getKnowledgeLimit() != null) {
            current.setKnowledgeLimit(config.getKnowledgeLimit());
        }
        current.setUpdatedAt(LocalDateTime.now());
        aiConfigMapper.updateById(current);
        return current;
    }
}
