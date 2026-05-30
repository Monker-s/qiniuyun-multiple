package com.contentpublish.adapter;

import com.contentpublish.dto.PlatformFormatRules;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class BilibiliAdapter extends AbstractPlatformAdapter {

    @Override
    public String getPlatformCode() { return "BILIBILI"; }

    @Override
    public String getPlatformName() { return "B站专栏"; }

    @Override
    public PlatformFormatRules getFormatRules() {
        PlatformFormatRules r = new PlatformFormatRules();
        r.setPlatformCode("BILIBILI");
        r.setPlatformName("B站专栏");
        r.setMaxTitleLength(100);
        r.setMaxContentLength(20000);
        r.setAllowedHtmlTags(List.of("h2","h3","p","strong","em","u","s",
                "img","a","ul","ol","li","blockquote","pre","code","br","hr"));
        r.setSupportVideo(true);
        r.setSupportEmbedCode(true);
        r.setImageRatio("自由");
        r.setMaxImageSizeMB(20);
        r.setMaxVideoSizeMB(8192);
        r.setMaxVideoDurationSec(36000);
        r.setVideoFormats(List.of("mp4","flv","avi"));
        r.setLineBreakStyle("BR");
        return r;
    }

    @Override
    public String convertToHtml(String tiptapJson, String title) {
        String html = super.convertToHtml(tiptapJson, title);
        return html.replaceAll("<h1>", "<h2>").replaceAll("</h1>", "</h2>");
    }
}
