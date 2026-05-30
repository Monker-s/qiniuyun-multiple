package com.contentpublish.controller;

import com.contentpublish.common.Result;
import com.contentpublish.dto.PlatformConvertResultDTO;
import com.contentpublish.service.AIService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/apply-template")
    public Result<PlatformConvertResultDTO> applyTemplate(@RequestBody Map<String, Object> req) {
        Long contentId = Long.valueOf(req.get("contentId").toString());
        Long templateId = Long.valueOf(req.get("templateId").toString());
        String platformCode = (String) req.get("targetPlatformCode");
        return Result.ok(aiService.applyTemplate(contentId, templateId, platformCode));
    }

    @PostMapping("/custom-adapt")
    public Result<PlatformConvertResultDTO> customAdapt(@RequestBody Map<String, String> req) {
        Long contentId = Long.valueOf(req.get("contentId"));
        String platformCode = req.get("targetPlatformCode");
        String systemPrompt = req.getOrDefault("systemPrompt", "");
        String userPrompt = req.getOrDefault("userPrompt", "");
        return Result.ok(aiService.customAdapt(contentId, platformCode, systemPrompt, userPrompt));
    }
}
