package com.contentpublish.dto;

import lombok.Data;
import java.util.List;

@Data
public class PublishRequestDTO {
    private Long contentId;
    private String platformCode;
    private String title;
    private String adaptedHtml;

    @Data
    public static class BatchPublishRequest {
        private Long contentId;
        private List<PlatformItem> platforms;
    }

    @Data
    public static class PlatformItem {
        private String platformCode;
        private String title;
        private String adaptedHtml;
    }
}
