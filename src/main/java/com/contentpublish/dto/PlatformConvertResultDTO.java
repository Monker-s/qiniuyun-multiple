package com.contentpublish.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class PlatformConvertResultDTO {
    private String adaptedHtml;
    private int wordCount;
    private List<Map<String, String>> platformWarnings;
    private String platformCode;
}
