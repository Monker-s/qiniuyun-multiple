package com.contentpublish.controller;

import com.contentpublish.common.Result;
import com.contentpublish.dto.PlatformConvertResultDTO;
import com.contentpublish.dto.PlatformFormatRules;
import com.contentpublish.service.PlatformService;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/platform")
public class PlatformController {

    private final PlatformService platformService;

    public PlatformController(PlatformService platformService) {
        this.platformService = platformService;
    }

    @PostMapping("/convert")
    public Result<PlatformConvertResultDTO> convert(@RequestBody Map<String, String> req) {
        String tiptapJson = req.get("tiptapJson");
        String platformCode = req.get("platformCode");
        String title = req.getOrDefault("title", "");
        return Result.ok(platformService.convert(tiptapJson, platformCode, title));
    }

    @GetMapping("/{code}/rules")
    public Result<PlatformFormatRules> getRules(@PathVariable String code) {
        return Result.ok(platformService.getRules(code));
    }

    @GetMapping("/rules")
    public Result<Map<String, PlatformFormatRules>> getAllRules() {
        return Result.ok(platformService.getAllRules());
    }

    @GetMapping("/video-limits")
    public Result<Map<String, Map<String, Object>>> getVideoLimits() {
        return Result.ok(platformService.getVideoLimits());
    }
}
