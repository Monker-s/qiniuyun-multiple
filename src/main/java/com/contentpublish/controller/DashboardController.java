package com.contentpublish.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contentpublish.common.Result;
import com.contentpublish.dto.DashboardStatsDTO;
import com.contentpublish.entity.PublishLog;
import com.contentpublish.service.DashboardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public Result<DashboardStatsDTO> getStats() {
        return Result.ok(dashboardService.getStats());
    }

    @GetMapping("/history")
    public Result<Page<PublishLog>> getHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String status) {
        return Result.ok(dashboardService.getHistory(page, size, platform, status));
    }
}
