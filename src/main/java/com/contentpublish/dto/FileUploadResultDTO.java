package com.contentpublish.dto;

import lombok.Data;

@Data
public class FileUploadResultDTO {
    private String url;
    private String thumbnail;
    private Integer width;
    private Integer height;
    private Long fileSize;
    private Long compressedSize;
    private Integer duration;
}
