package com.contentpublish.adapter;

import com.contentpublish.converter.TiptapToHtmlConverter;
import com.contentpublish.dto.PlatformFormatRules;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractPlatformAdapter implements PlatformAdapter {

    private final TiptapToHtmlConverter converter = new TiptapToHtmlConverter();

    @Override
    public String convertToHtml(String tiptapJson, String title) {
        String rawHtml = converter.convert(tiptapJson, title);
        return postProcess(rawHtml, getFormatRules());
    }

    protected String postProcess(String html, PlatformFormatRules rules) {
        if (rules.getAllowedHtmlTags() == null || rules.getAllowedHtmlTags().isEmpty()) {
            return html;
        }
        Set<String> allowed = rules.getAllowedHtmlTags().stream()
                .map(String::toLowerCase).collect(Collectors.toSet());
        if (allowed.contains("img")) {
            return html; // keep images
        }
        return html.replaceAll("<img[^>]*>", "");
    }
}
