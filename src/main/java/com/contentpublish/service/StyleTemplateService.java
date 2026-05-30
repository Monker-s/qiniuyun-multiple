package com.contentpublish.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contentpublish.entity.StyleTemplate;
import java.util.Map;

public interface StyleTemplateService {
    Page<StyleTemplate> list(int page, int size);
    StyleTemplate getById(Long id);
    StyleTemplate create(StyleTemplate template);
    StyleTemplate update(StyleTemplate template);
    void delete(Long id);
    Map<String, Object> analyzeText(String text);
    Map<String, Object> analyzeImage(String imageUrl);
}
