package com.contentpublish.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("publish_log")
public class PublishLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long contentId;

    private String platformCode;

    private String status;

    private String platformUrl;

    private String errorMsg;

    private String screenshot;

    private LocalDateTime retractedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
