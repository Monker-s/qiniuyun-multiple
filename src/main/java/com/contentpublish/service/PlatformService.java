package com.contentpublish.service;

import com.contentpublish.adapter.PlatformAdapter;
import com.contentpublish.dto.PlatformConvertResultDTO;
import com.contentpublish.dto.PlatformFormatRules;
import java.util.Collection;
import java.util.Map;

public interface PlatformService {
    Collection<PlatformAdapter> getAdapters();
    PlatformAdapter getAdapter(String platformCode);
    PlatformConvertResultDTO convert(String tiptapJson, String platformCode, String title);
    PlatformFormatRules getRules(String platformCode);
    Map<String, PlatformFormatRules> getAllRules();
    Map<String, Map<String, Object>> getVideoLimits();
}
