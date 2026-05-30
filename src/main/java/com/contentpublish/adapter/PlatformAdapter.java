package com.contentpublish.adapter;

import com.contentpublish.dto.PlatformFormatRules;

public interface PlatformAdapter {
    String getPlatformCode();
    String getPlatformName();
    PlatformFormatRules getFormatRules();
    String convertToHtml(String tiptapJson, String title);
}
