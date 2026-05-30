package com.contentpublish.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.contentpublish.dto.DashboardStatsDTO;
import com.contentpublish.entity.PublishLog;
import com.contentpublish.mapper.PublishLogMapper;
import com.contentpublish.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl extends ServiceImpl<PublishLogMapper, PublishLog>
        implements DashboardService {

    @Override
    public DashboardStatsDTO getStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        List<PublishLog> all = list();
        long success = all.stream().filter(l -> "SUCCESS".equals(l.getStatus())).count();
        long failed = all.stream().filter(l -> "FAILED".equals(l.getStatus())).count();
        long retracted = all.stream().filter(l -> "RETRACTED".equals(l.getStatus())).count();

        stats.setTotalPublishes(all.size());
        stats.setSuccessPublishes(success + retracted); // 撤回也算发布过
        stats.setFailedPublishes(failed);

        Map<String, Long> perPlatform = new LinkedHashMap<>();
        for (PublishLog log : all) {
            perPlatform.merge(log.getPlatformCode(), 1L, Long::sum);
        }
        stats.setPerPlatform(perPlatform);
        return stats;
    }

    @Override
    public Page<PublishLog> getHistory(int page, int size, String platform, String status) {
        LambdaQueryWrapper<PublishLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(platform)) {
            wrapper.eq(PublishLog::getPlatformCode, platform);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(PublishLog::getStatus, status);
        }
        wrapper.orderByDesc(PublishLog::getCreatedAt);
        return page(new Page<>(page, size), wrapper);
    }
}
