package com.contentpublish.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contentpublish.common.Result;
import com.contentpublish.dto.ContentSaveDTO;
import com.contentpublish.entity.Content;
import com.contentpublish.service.ContentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    @PostMapping
    public Result<Content> save(@RequestBody ContentSaveDTO dto) {
        return Result.ok(contentService.save(dto));
    }

    @GetMapping("/{id}")
    public Result<Content> getById(@PathVariable Long id) {
        return Result.ok(contentService.getById(id));
    }

    @GetMapping
    public Result<Page<Content>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.ok(contentService.list(page, size, keyword));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        contentService.delete(id);
        return Result.ok();
    }
}
