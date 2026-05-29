package com.contentpublish.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("style_template")
public class StyleTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String icon;

    private String description;

    private String referenceType;

    private String referenceSample;

    private String referenceImage;

    private String systemPrompt;

    private String extractedStyle;

    private Boolean isPreset;

    private Long userId;

    private Integer orderNo;

    private Integer useCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
