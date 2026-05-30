package com.contentpublish.adapter;

import com.contentpublish.dto.PlatformFormatRules;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ZhihuAdapter extends AbstractPlatformAdapter {

    @Override
    public String getPlatformCode() { return "ZHIHU"; }

    @Override
    public String getPlatformName() { return "知乎"; }

    @Override
    public PlatformFormatRules getFormatRules() {
        PlatformFormatRules r = new PlatformFormatRules();
        r.setPlatformCode("ZHIHU");
        r.setPlatformName("知乎");
        r.setMaxTitleLength(100);
        r.setMaxContentLength(50000);
        r.setAllowedHtmlTags(List.of("h1","h2","h3","p","strong","em","u","s",
                "img","a","ul","ol","li","blockquote","pre","code","br","hr"));
        r.setSupportVideo(false);
        r.setSupportEmbedCode(true);
        r.setImageRatio("自由");
        r.setMaxImageSizeMB(20);
        r.setMaxVideoSizeMB(0);
        r.setMaxVideoDurationSec(0);
        r.setVideoFormats(List.of());
        r.setVideoNotSupportedReason("知乎图文不支持视频");
        r.setLineBreakStyle("P");
        return r;
    }
}
