package com.contentpublish.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_platform")
public class UserPlatform {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String platformCode;

    private String platformName;

    private String loginUrl;

    private String editorUrl;

    private Boolean isBuiltin;

    private Boolean isActive;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
