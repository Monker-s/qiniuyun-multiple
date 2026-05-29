package com.contentpublish.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("content_platform_version")
public class ContentPlatformVersion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long contentId;

    private String platformCode;

    private String adaptedHtml;

    private Long appliedTemplateId;

    private Boolean isEdited;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
