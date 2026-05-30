package com.contentpublish.dto;

import lombok.Data;
import java.util.List;

@Data
public class PlatformFormatRules {
    private String platformCode;
    private String platformName;
    private int maxTitleLength;
    private int maxContentLength;
    private List<String> allowedHtmlTags;
    private boolean supportVideo;
    private boolean supportEmbedCode;
    private String imageRatio;
    private int maxImageSizeMB;
    private long maxVideoSizeMB;
    private int maxVideoDurationSec;
    private List<String> videoFormats;
    private String videoNotSupportedReason;
    private String lineBreakStyle;
}
