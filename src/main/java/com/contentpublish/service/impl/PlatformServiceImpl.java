package com.contentpublish.service.impl;

import com.contentpublish.adapter.PlatformAdapter;
import com.contentpublish.dto.PlatformConvertResultDTO;
import com.contentpublish.dto.PlatformFormatRules;
import com.contentpublish.service.PlatformService;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class PlatformServiceImpl implements PlatformService {

    private final Map<String, PlatformAdapter> adapterMap = new LinkedHashMap<>();

    public PlatformServiceImpl(List<PlatformAdapter> adapters) {
        for (PlatformAdapter a : adapters) {
            adapterMap.put(a.getPlatformCode(), a);
        }
    }

    @Override
    public Collection<PlatformAdapter> getAdapters() {
        return adapterMap.values();
    }

    @Override
    public PlatformAdapter getAdapter(String platformCode) {
        PlatformAdapter a = adapterMap.get(platformCode.toUpperCase());
        if (a == null) {
            throw new IllegalArgumentException("不支持的平台: " + platformCode);
        }
        return a;
    }

    @Override
    public PlatformConvertResultDTO convert(String tiptapJson, String platformCode, String title) {
        PlatformAdapter adapter = getAdapter(platformCode);
        String html = adapter.convertToHtml(tiptapJson, title);
        PlatformFormatRules rules = adapter.getFormatRules();

        PlatformConvertResultDTO result = new PlatformConvertResultDTO();
        result.setAdaptedHtml(html);
        result.setPlatformCode(adapter.getPlatformCode());

        // word count (strip HTML tags)
        int wordCount = html.replaceAll("<[^>]+>", "").replaceAll("\\s+", "").length();
        result.setWordCount(wordCount);

        List<Map<String, String>> warnings = new ArrayList<>();
        if (wordCount > rules.getMaxContentLength()) {
            warnings.add(Map.of("type", "word_exceed",
                    "message", "字数超出限制 " + wordCount + "/" + rules.getMaxContentLength()));
        }
        result.setPlatformWarnings(warnings);
        return result;
    }

    @Override
    public PlatformFormatRules getRules(String platformCode) {
        return getAdapter(platformCode).getFormatRules();
    }

    @Override
    public Map<String, PlatformFormatRules> getAllRules() {
        Map<String, PlatformFormatRules> map = new LinkedHashMap<>();
        for (PlatformAdapter a : adapterMap.values()) {
            map.put(a.getPlatformCode(), a.getFormatRules());
        }
        return map;
    }

    @Override
    public Map<String, Map<String, Object>> getVideoLimits() {
        Map<String, Map<String, Object>> limits = new LinkedHashMap<>();
        for (PlatformAdapter a : adapterMap.values()) {
            PlatformFormatRules r = a.getFormatRules();
            Map<String, Object> info = new LinkedHashMap<>();
            info.put("supported", r.isSupportVideo());
            if (r.isSupportVideo()) {
                info.put("maxSizeMB", r.getMaxVideoSizeMB());
                info.put("maxDurationSec", r.getMaxVideoDurationSec());
                info.put("formats", r.getVideoFormats());
            } else {
                info.put("reason", r.getVideoNotSupportedReason());
            }
            limits.put(a.getPlatformCode(), info);
        }
        return limits;
    }
}
