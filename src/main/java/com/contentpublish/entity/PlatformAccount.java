package com.contentpublish.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("platform_account")
public class PlatformAccount {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userPlatformId;

    private String cookieFilePath;

    private String displayName;

    private Boolean isActive;

    private LocalDateTime lastLoginAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
