package com.contentpublish.adapter;

import com.contentpublish.dto.PlatformFormatRules;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class WeChatAdapter extends AbstractPlatformAdapter {

    @Override
    public String getPlatformCode() { return "WECHAT"; }

    @Override
    public String getPlatformName() { return "微信公众号"; }

    @Override
    public PlatformFormatRules getFormatRules() {
        PlatformFormatRules r = new PlatformFormatRules();
        r.setPlatformCode("WECHAT");
        r.setPlatformName("微信公众号");
        r.setMaxTitleLength(64);
        r.setMaxContentLength(20000);
        r.setAllowedHtmlTags(List.of("h1","h2","h3","p","strong","em","u","s",
                "img","a","ul","ol","li","blockquote","pre","code","br","hr","section"));
        r.setSupportVideo(true);
        r.setSupportEmbedCode(false);
        r.setImageRatio("16:9或4:3");
        r.setMaxImageSizeMB(10);
        r.setMaxVideoSizeMB(1024);
        r.setMaxVideoDurationSec(1800);
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
