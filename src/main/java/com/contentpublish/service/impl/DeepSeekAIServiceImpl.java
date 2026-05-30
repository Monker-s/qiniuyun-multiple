package com.contentpublish.service.impl;

import com.contentpublish.config.DeepSeekConfig;
import com.contentpublish.converter.TiptapToHtmlConverter;
import com.contentpublish.dto.PlatformConvertResultDTO;
import com.contentpublish.entity.Content;
import com.contentpublish.entity.ContentPlatformVersion;
import com.contentpublish.entity.StyleTemplate;
import com.contentpublish.mapper.ContentMapper;
import com.contentpublish.mapper.ContentPlatformVersionMapper;
import com.contentpublish.mapper.StyleTemplateMapper;
import com.contentpublish.service.AIService;
import com.contentpublish.service.PlatformService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class DeepSeekAIServiceImpl implements AIService {

    private final DeepSeekConfig config;
    private final RestTemplate restTemplate;
    private final ContentMapper contentMapper;
    private final StyleTemplateMapper templateMapper;
    private final ContentPlatformVersionMapper versionMapper;
    private final PlatformService platformService;
    private final TiptapToHtmlConverter htmlConverter = new TiptapToHtmlConverter();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public DeepSeekAIServiceImpl(DeepSeekConfig config, RestTemplate restTemplate,
                                  ContentMapper contentMapper, StyleTemplateMapper templateMapper,
                                  ContentPlatformVersionMapper versionMapper, PlatformService platformService) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.contentMapper = contentMapper;
        this.templateMapper = templateMapper;
        this.versionMapper = versionMapper;
        this.platformService = platformService;
    }

    @Override
    public PlatformConvertResultDTO applyTemplate(Long contentId, Long templateId, String targetPlatformCode) {
        Content content = contentMapper.selectById(contentId);
        if (content == null) throw new IllegalArgumentException("内容不存在");

        StyleTemplate template = templateMapper.selectById(templateId);
        if (template == null) throw new IllegalArgumentException("模板不存在");

        String originalText = extractText(content.getTiptapJson());
        String systemPrompt = template.getSystemPrompt();
        String userPrompt = "请将以下内容改写为目标平台风格:\n\n" + originalText;

        String adaptedText = callDeepSeek(systemPrompt, userPrompt);
        String adaptedHtml = textToSimpleHtml(adaptedText);

        // save platform version
        savePlatformVersion(contentId, targetPlatformCode, adaptedHtml, templateId);

        // update template use count
        template.setUseCount((template.getUseCount() == null ? 0 : template.getUseCount()) + 1);
        templateMapper.updateById(template);

        PlatformConvertResultDTO result = new PlatformConvertResultDTO();
        result.setAdaptedHtml(adaptedHtml);
        result.setPlatformCode(targetPlatformCode);
        result.setWordCount(adaptedText.replaceAll("\\s+", "").length());
        result.setPlatformWarnings(List.of());
        return result;
    }

    @Override
    public PlatformConvertResultDTO customAdapt(Long contentId, String targetPlatformCode,
                                                  String systemPrompt, String userPrompt) {
        Content content = contentMapper.selectById(contentId);
        if (content == null) throw new IllegalArgumentException("内容不存在");

        String originalText = extractText(content.getTiptapJson());
        String finalUserPrompt = userPrompt + "\n\n原文:\n" + originalText;

        String adaptedText = callDeepSeek(systemPrompt, finalUserPrompt);
        String adaptedHtml = textToSimpleHtml(adaptedText);

        savePlatformVersion(contentId, targetPlatformCode, adaptedHtml, null);

        PlatformConvertResultDTO result = new PlatformConvertResultDTO();
        result.setAdaptedHtml(adaptedHtml);
        result.setPlatformCode(targetPlatformCode);
        result.setWordCount(adaptedText.replaceAll("\\s+", "").length());
        result.setPlatformWarnings(List.of());
        return result;
    }

    @Override
    public Map<String, Object> analyzeText(String text) {
        String systemPrompt = "你是文本风格分析专家。分析给定文本的写作风格，返回JSON格式，包含: tone(语气), emojiDensity(emoji密度high/medium/low/none), paragraphLength(段落长度very_short/short/medium/long), signaturePhrases(特征短语数组), endingStyle(结尾风格), targetAudience(目标读者), suggestedTemplateName(建议模板名), generatedSystemPrompt(为AI生成的SystemPrompt，用于后续模仿此风格)。只返回JSON，不要其他内容。";
        String response = callDeepSeek(systemPrompt, "分析以下文本的写作风格:\n\n" + text);
        return parseAnalysisResponse(response);
    }

    @Override
    public Map<String, Object> analyzeImage(String imageUrl) {
        String systemPrompt = "你是视觉排版分析专家。分析图片中的内容排版和文字风格，返回JSON格式，包含: visualStyle(视觉风格描述), tone(语气), emojiDensity, paragraphLength, signaturePhrases(特征短语数组), suggestedTemplateName(建议模板名), generatedSystemPrompt(为AI生成的SystemPrompt)。只返回JSON，不要其他内容。";
        String userPrompt = "分析这张图片中的内容风格和排版特征。图片URL: " + imageUrl;
        String response = callDeepSeek(systemPrompt, userPrompt);
        return parseAnalysisResponse(response);
    }

    private String callDeepSeek(String systemPrompt, String userPrompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", systemPrompt));
            messages.add(Map.of("role", "user", "content", userPrompt));

            Map<String, Object> body = new HashMap<>();
            body.put("model", config.getModel());
            body.put("messages", messages);
            body.put("temperature", 0.7);
            body.put("max_tokens", 4096);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(config.getApiUrl(), request, Map.class);

            Map<String, Object> respBody = response.getBody();
            if (respBody == null) throw new RuntimeException("DeepSeek返回为空");

            List<Map<String, Object>> choices = (List<Map<String, Object>>) respBody.get("choices");
            if (choices == null || choices.isEmpty()) throw new RuntimeException("DeepSeek返回无choices");

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            throw new RuntimeException("AI调用失败: " + e.getMessage());
        }
    }

    private String extractText(String tiptapJson) {
        if (tiptapJson == null) return "";
        String html = htmlConverter.convert(tiptapJson, "");
        return html.replaceAll("<[^>]+>", "").replaceAll("&amp;", "&")
                .replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&quot;", "\"");
    }

    private String textToSimpleHtml(String text) {
        StringBuilder sb = new StringBuilder();
        for (String line : text.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.startsWith("# ")) {
                sb.append("<h2>").append(escapeHtml(trimmed.substring(2))).append("</h2>\n");
            } else if (trimmed.startsWith("## ")) {
                sb.append("<h3>").append(escapeHtml(trimmed.substring(3))).append("</h3>\n");
            } else {
                sb.append("<p>").append(escapeHtml(trimmed)).append("</p>\n");
            }
        }
        return sb.toString();
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void savePlatformVersion(Long contentId, String platformCode, String html, Long templateId) {
        ContentPlatformVersion ver = versionMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ContentPlatformVersion>()
                .eq(ContentPlatformVersion::getContentId, contentId)
                .eq(ContentPlatformVersion::getPlatformCode, platformCode));
        if (ver == null) {
            ver = new ContentPlatformVersion();
            ver.setContentId(contentId);
            ver.setPlatformCode(platformCode);
        }
        ver.setAdaptedHtml(html);
        ver.setAppliedTemplateId(templateId);
        ver.setIsEdited(false);
        if (ver.getId() == null) {
            versionMapper.insert(ver);
        } else {
            versionMapper.updateById(ver);
        }
    }

    private Map<String, Object> parseAnalysisResponse(String response) {
        try {
            String json = response.trim();
            if (json.startsWith("```")) {
                json = json.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
            }
            return jsonMapper.readValue(json, Map.class);
        } catch (Exception e) {
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("rawResponse", response);
            fallback.put("suggestedTemplateName", "自定义模板");
            fallback.put("generatedSystemPrompt", "请模仿以下风格进行改写");
            return fallback;
        }
    }
}
