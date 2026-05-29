package com.contentpublish.common;

public enum PlatformEnum {
    WECHAT("WECHAT", "微信公众号"),
    ZHIHU("ZHIHU", "知乎"),
    BILIBILI("BILIBILI", "B站专栏"),
    XHS("XHS", "小红书"),
    TOUTIAO("TOUTIAO", "今日头条");

    private final String code;
    private final String name;

    PlatformEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() { return code; }
    public String getName() { return name; }

    public static PlatformEnum fromCode(String code) {
        for (PlatformEnum p : values()) {
            if (p.code.equalsIgnoreCase(code)) return p;
        }
        throw new IllegalArgumentException("Unknown platform: " + code);
    }
}
