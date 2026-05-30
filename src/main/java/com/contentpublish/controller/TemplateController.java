package com.contentpublish.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contentpublish.common.Result;
import com.contentpublish.entity.StyleTemplate;
import com.contentpublish.service.StyleTemplateService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {

    private final StyleTemplateService templateService;

    public TemplateController(StyleTemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public Result<Page<StyleTemplate>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(templateService.list(page, size));
    }

    @GetMapping("/{id}")
    public Result<StyleTemplate> getById(@PathVariable Long id) {
        return Result.ok(templateService.getById(id));
    }

    @PostMapping
    public Result<StyleTemplate> create(@RequestBody StyleTemplate template) {
        return Result.ok(templateService.create(template));
    }

    @PutMapping("/{id}")
    public Result<StyleTemplate> update(@PathVariable Long id, @RequestBody StyleTemplate template) {
        template.setId(id);
        return Result.ok(templateService.update(template));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        templateService.delete(id);
        return Result.ok();
    }

    @PostMapping("/analyze-text")
    public Result<Map<String, Object>> analyzeText(@RequestBody Map<String, String> req) {
        return Result.ok(templateService.analyzeText(req.get("text")));
    }

    @PostMapping("/analyze-image")
    public Result<Map<String, Object>> analyzeImage(@RequestBody Map<String, String> req) {
        return Result.ok(templateService.analyzeImage(req.get("imageUrl")));
    }
}
