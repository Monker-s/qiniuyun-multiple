package com.contentpublish.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("upload_file")
public class UploadFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String originalName;

    private String storedPath;

    private String fileType;

    private Long fileSize;

    private Long compressedSize;

    private Integer width;

    private Integer height;

    private Integer duration;

    private String thumbnailPath;

    private String uploadStatus;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
