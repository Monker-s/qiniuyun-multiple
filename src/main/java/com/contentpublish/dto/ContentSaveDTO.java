package com.contentpublish.dto;

import lombok.Data;

@Data
public class ContentSaveDTO {
    private Long id;
    private String title;
    private String tiptapJson;
    private String coverImage;
}
