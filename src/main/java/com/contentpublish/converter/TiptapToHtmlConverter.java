package com.contentpublish.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Iterator;
import java.util.Map;

public class TiptapToHtmlConverter {

    private final ObjectMapper mapper = new ObjectMapper();

    public String convert(String tiptapJson, String title) {
        if (tiptapJson == null || tiptapJson.isBlank()) {
            return wrapWithTitle("", title);
        }
        try {
            JsonNode root = mapper.readTree(tiptapJson);
            StringBuilder sb = new StringBuilder();
            if (title != null && !title.isBlank()) {
                sb.append("<h1>").append(escapeHtml(title)).append("</h1>\n");
            } else {
                sb.append("<h1>").append(escapeHtml(title == null ? "" : title)).append("</h1>\n");
            }
            JsonNode content = root.get("content");
            if (content != null && content.isArray()) {
                for (JsonNode node : content) {
                    renderNode(node, sb);
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return wrapWithTitle("<p>" + escapeHtml(tiptapJson) + "</p>", title);
        }
    }

    private void renderNode(JsonNode node, StringBuilder sb) {
        if (node == null) return;
        String type = node.has("type") ? node.get("type").asText() : "";

        switch (type) {
            case "heading" -> {
                int level = node.has("attrs") ? node.get("attrs").get("level").asInt(1) : 1;
                sb.append("<h").append(level).append(">");
                renderContent(node, sb);
                sb.append("</h").append(level).append(">\n");
            }
            case "paragraph" -> {
                sb.append("<p>");
                renderContent(node, sb);
                sb.append("</p>\n");
            }
            case "bulletList" -> {
                sb.append("<ul>\n");
                renderContent(node, sb);
                sb.append("</ul>\n");
            }
            case "orderedList" -> {
                sb.append("<ol>\n");
                renderContent(node, sb);
                sb.append("</ol>\n");
            }
            case "listItem" -> {
                sb.append("<li>");
                renderContent(node, sb);
                sb.append("</li>\n");
            }
            case "blockquote" -> {
                sb.append("<blockquote>");
                renderContent(node, sb);
                sb.append("</blockquote>\n");
            }
            case "codeBlock" -> {
                sb.append("<pre><code>");
                renderTextContent(node, sb);
                sb.append("</code></pre>\n");
            }
            case "image" -> {
                String src = node.has("attrs") ? node.get("attrs").get("src").asText("") : "";
                sb.append("<img src=\"").append(escapeAttr(src)).append("\" />");
            }
            case "horizontalRule" -> sb.append("<hr />\n");
            case "hardBreak" -> sb.append("<br />");
            case "text" -> renderTextWithMarks(node, sb);
            default -> {
                JsonNode childContent = node.get("content");
                if (childContent != null && childContent.isArray()) {
                    for (JsonNode child : childContent) {
                        renderNode(child, sb);
                    }
                }
            }
        }
    }

    private void renderContent(JsonNode node, StringBuilder sb) {
        JsonNode content = node.get("content");
        if (content != null && content.isArray()) {
            for (JsonNode child : content) {
                renderNode(child, sb);
            }
        }
    }

    private void renderTextContent(JsonNode node, StringBuilder sb) {
        JsonNode content = node.get("content");
        if (content != null && content.isArray()) {
            for (JsonNode child : content) {
                if ("text".equals(child.has("type") ? child.get("type").asText() : "")) {
                    String text = child.has("text") ? child.get("text").asText() : "";
                    sb.append(escapeHtml(text));
                }
            }
        }
    }

    private void renderTextWithMarks(JsonNode node, StringBuilder sb) {
        String text = node.has("text") ? node.get("text").asText() : "";
        JsonNode marks = node.get("marks");
        if (marks == null || !marks.isArray() || marks.size() == 0) {
            sb.append(escapeHtml(text));
            return;
        }
        StringBuilder openTags = new StringBuilder();
        StringBuilder closeTags = new StringBuilder();
        for (JsonNode mark : marks) {
            String markType = mark.get("type").asText();
            switch (markType) {
                case "bold" -> { openTags.append("<strong>"); closeTags.insert(0, "</strong>"); }
                case "italic" -> { openTags.append("<em>"); closeTags.insert(0, "</em>"); }
                case "underline" -> { openTags.append("<u>"); closeTags.insert(0, "</u>"); }
                case "strike" -> { openTags.append("<s>"); closeTags.insert(0, "</s>"); }
                case "code" -> { openTags.append("<code>"); closeTags.insert(0, "</code>"); }
                case "link" -> {
                    String href = mark.has("attrs") ? mark.get("attrs").get("href").asText("#") : "#";
                    openTags.append("<a href=\"").append(escapeAttr(href)).append("\">");
                    closeTags.insert(0, "</a>");
                }
            }
        }
        sb.append(openTags).append(escapeHtml(text)).append(closeTags);
    }

    private String wrapWithTitle(String body, String title) {
        return "<h1>" + escapeHtml(title != null ? title : "") + "</h1>\n" + body;
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }

    private String escapeAttr(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("\"", "&quot;");
    }
}
