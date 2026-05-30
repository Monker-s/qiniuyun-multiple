package com.contentpublish.adapter;

import com.contentpublish.dto.PlatformFormatRules;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class XiaohongshuAdapter extends AbstractPlatformAdapter {

    @Override
    public String getPlatformCode() { return "XHS"; }

    @Override
    public String getPlatformName() { return "小红书"; }

    @Override
    public PlatformFormatRules getFormatRules() {
        PlatformFormatRules r = new PlatformFormatRules();
        r.setPlatformCode("XHS");
        r.setPlatformName("小红书");
        r.setMaxTitleLength(20);
        r.setMaxContentLength(1000);
        r.setAllowedHtmlTags(List.of("p","strong","em","img","br"));
        r.setSupportVideo(false);
        r.setSupportEmbedCode(false);
        r.setImageRatio("3:4");
        r.setMaxImageSizeMB(10);
        r.setMaxVideoSizeMB(0);
        r.setMaxVideoDurationSec(0);
        r.setVideoFormats(List.of());
        r.setVideoNotSupportedReason("小红书图文不支持视频");
        r.setLineBreakStyle("BR");
        return r;
    }

    @Override
    public String convertToHtml(String tiptapJson, String title) {
        String html = super.convertToHtml(tiptapJson, title);
        html = html.replaceAll("<h[1-6][^>]*>", "<p><strong>")
                   .replaceAll("</h[1-6]>", "</strong></p>");
        html = html.replaceAll("<ul[^>]*>|<ol[^>]*>", "").replaceAll("</ul>|</ol>", "");
        html = html.replaceAll("</li>", "<br>").replaceAll("<li[^>]*>", "- ");
        html = html.replaceAll("<blockquote[^>]*>", "<p>").replaceAll("</blockquote>", "</p>");
        html = html.replaceAll("<pre[^>]*>|<code[^>]*>|</code>|</pre>", "");
        html = html.replaceAll("</p>\\s*<br\\s*/?>", "</p>");
        return html;
    }
}
