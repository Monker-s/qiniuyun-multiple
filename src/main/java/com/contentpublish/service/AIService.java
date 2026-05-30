package com.contentpublish.service;

import com.contentpublish.dto.PlatformConvertResultDTO;
import java.util.Map;

public interface AIService {
    PlatformConvertResultDTO applyTemplate(Long contentId, Long templateId, String targetPlatformCode);
    PlatformConvertResultDTO customAdapt(Long contentId, String targetPlatformCode, String systemPrompt, String userPrompt);
    Map<String, Object> analyzeText(String text);
    Map<String, Object> analyzeImage(String imageUrl);
}
