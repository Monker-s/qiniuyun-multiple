package com.contentpublish.dto;

import lombok.Data;
import java.util.Map;

@Data
public class DashboardStatsDTO {
    private long totalPublishes;
    private long successPublishes;
    private long failedPublishes;
    private Map<String, Long> perPlatform;
}
