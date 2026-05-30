package com.contentpublish.controller;

import com.contentpublish.common.Result;
import com.contentpublish.dto.PublishRequestDTO;
import com.contentpublish.dto.PublishResultDTO;
import com.contentpublish.service.PublishService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PublishController {

    private final PublishService publishService;

    public PublishController(PublishService publishService) {
        this.publishService = publishService;
    }

    @PostMapping("/platform/publish")
    public Result<PublishResultDTO> publish(@RequestBody PublishRequestDTO req) {
        return Result.ok(publishService.publish(req));
    }

    @PostMapping("/platform/batch-publish")
    public Result<PublishResultDTO> batchPublish(@RequestBody PublishRequestDTO.BatchPublishRequest req) {
        return Result.ok(publishService.batchPublish(req));
    }

    @GetMapping("/platform/publish/{taskId}/progress")
    public SseEmitter getProgress(@PathVariable String taskId) {
        return publishService.getProgress(taskId);
    }

    @PostMapping("/publish/{publishLogId}/retract")
    public Result<Void> retract(@PathVariable Long publishLogId) {
        publishService.retract(publishLogId);
        return Result.ok();
    }

    @PostMapping("/publish/{contentId}/retract-all")
    public Result<List<Long>> retractAll(@PathVariable Long contentId) {
        return Result.ok(publishService.retractAll(contentId));
    }
}
