package com.contentpublish.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.contentpublish.dto.DashboardStatsDTO;
import com.contentpublish.entity.PublishLog;

public interface DashboardService {
    DashboardStatsDTO getStats();
    Page<PublishLog> getHistory(int page, int size, String platform, String status);
}
