package com.contentpublish.adapter;

import com.contentpublish.dto.PlatformFormatRules;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ToutiaoAdapter extends AbstractPlatformAdapter {

    @Override
    public String getPlatformCode() { return "TOUTIAO"; }

    @Override
    public String getPlatformName() { return "今日头条"; }

    @Override
    public PlatformFormatRules getFormatRules() {
        PlatformFormatRules r = new PlatformFormatRules();
        r.setPlatformCode("TOUTIAO");
        r.setPlatformName("今日头条");
        r.setMaxTitleLength(30);
        r.setMaxContentLength(20000);
        r.setAllowedHtmlTags(List.of("h2","h3","p","strong","em","u",
                "img","a","ul","ol","li","blockquote","br"));
        r.setSupportVideo(true);
        r.setSupportEmbedCode(false);
        r.setImageRatio("16:9");
        r.setMaxImageSizeMB(20);
        r.setMaxVideoSizeMB(4096);
        r.setMaxVideoDurationSec(3600);
        r.setVideoFormats(List.of("mp4"));
        r.setLineBreakStyle("P");
        return r;
    }

    @Override
    public String convertToHtml(String tiptapJson, String title) {
        String html = super.convertToHtml(tiptapJson, title);
        return html.replaceAll("<h1>", "<h2>").replaceAll("</h1>", "</h2>");
    }
}
