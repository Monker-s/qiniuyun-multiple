package com.contentpublish.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contentpublish.entity.StyleTemplate;
import com.contentpublish.mapper.StyleTemplateMapper;
import com.contentpublish.service.AIService;
import com.contentpublish.service.StyleTemplateService;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class StyleTemplateServiceImpl extends ServiceImpl<StyleTemplateMapper, StyleTemplate>
        implements StyleTemplateService {

    private final AIService aiService;

    public StyleTemplateServiceImpl(AIService aiService) {
        this.aiService = aiService;
    }

    @Override
    public Page<StyleTemplate> list(int page, int size) {
        LambdaQueryWrapper<StyleTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(StyleTemplate::getIsPreset)
               .orderByDesc(StyleTemplate::getUseCount);
        return page(new Page<>(page, size), wrapper);
    }

    @Override
    public StyleTemplate getById(Long id) {
        StyleTemplate t = super.getById(id);
        if (t == null) throw new IllegalArgumentException("模板不存在");
        return t;
    }

    @Override
    public StyleTemplate create(StyleTemplate template) {
        template.setIsPreset(false);
        template.setUseCount(0);
        save(template);
        return template;
    }

    @Override
    public StyleTemplate update(StyleTemplate template) {
        StyleTemplate existing = getById(template.getId());
        existing.setName(template.getName());
        existing.setIcon(template.getIcon());
        existing.setDescription(template.getDescription());
        existing.setSystemPrompt(template.getSystemPrompt());
        existing.setReferenceSample(template.getReferenceSample());
        existing.setReferenceImage(template.getReferenceImage());
        existing.setExtractedStyle(template.getExtractedStyle());
        updateById(existing);
        return existing;
    }

    @Override
    public void delete(Long id) {
        StyleTemplate t = getById(id);
        if (t.getIsPreset()) throw new IllegalArgumentException("系统预置模板不可删除");
        removeById(id);
    }

    @Override
    public Map<String, Object> analyzeText(String text) {
        return aiService.analyzeText(text);
    }

    @Override
    public Map<String, Object> analyzeImage(String imageUrl) {
        return aiService.analyzeImage(imageUrl);
    }
}
