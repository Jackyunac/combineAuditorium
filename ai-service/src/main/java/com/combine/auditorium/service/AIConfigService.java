package com.combine.auditorium.service;

import com.combine.auditorium.entity.AIConfig;

public interface AIConfigService {

    AIConfig getConfig();

    AIConfig updateConfig(AIConfig config);
}
