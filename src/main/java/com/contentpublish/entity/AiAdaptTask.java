package com.contentpublish.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_adapt_task")
public class AiAdaptTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long contentId;

    private String targetPlatformCode;

    private String presetTemplate;

    private String userPrompt;

    private String adaptedContent;

    private Integer tokensUsed;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
