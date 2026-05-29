-- =============================================
-- 多平台内容发布工具 - 数据库初始化脚本
-- MySQL 8.0+
-- =============================================

CREATE DATABASE IF NOT EXISTS content_publish
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE content_publish;

-- 内容主表
CREATE TABLE IF NOT EXISTS content (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(255) NOT NULL COMMENT '文章标题',
    tiptap_json LONGTEXT NOT NULL COMMENT 'Tiptap ProseMirror JSON',
    cover_image VARCHAR(500) DEFAULT NULL COMMENT '封面图URL',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT(1) DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_created_at (created_at),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容主表';

-- 平台版本表
CREATE TABLE IF NOT EXISTS content_platform_version (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    content_id          BIGINT NOT NULL COMMENT '内容ID',
    platform_code       VARCHAR(50) NOT NULL COMMENT '平台代码',
    adapted_html        LONGTEXT NOT NULL COMMENT '适配后HTML',
    applied_template_id BIGINT DEFAULT NULL COMMENT '使用的模板ID',
    is_edited           TINYINT(1) DEFAULT 0 COMMENT '用户是否手动微调',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_content_platform (content_id, platform_code),
    INDEX idx_content_id (content_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台版本表';

-- 发布记录表
CREATE TABLE IF NOT EXISTS publish_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    content_id      BIGINT NOT NULL COMMENT '内容ID',
    platform_code   VARCHAR(50) NOT NULL COMMENT '平台代码',
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING'
                    COMMENT 'PENDING/PREFLIGHT/FILLING/PUBLISHING/CONFIRMING/SUCCESS/FAILED/RETRACTING/RETRACTED',
    platform_url    VARCHAR(500) DEFAULT NULL COMMENT '发布后文章链接',
    error_msg       VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    screenshot      MEDIUMTEXT DEFAULT NULL COMMENT '截图(base64)',
    retracted_at    DATETIME DEFAULT NULL COMMENT '撤回时间',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_content_id (content_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发布记录表';

-- 用户平台表
CREATE TABLE IF NOT EXISTS user_platform (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    platform_code   VARCHAR(50) NOT NULL COMMENT '平台代码',
    platform_name   VARCHAR(100) NOT NULL COMMENT '平台显示名',
    login_url       VARCHAR(500) NOT NULL COMMENT '登录页URL',
    editor_url      VARCHAR(500) NOT NULL COMMENT '编辑器URL',
    is_builtin      TINYINT(1) DEFAULT 0 COMMENT '是否系统内置',
    is_active       TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    sort_order      INT DEFAULT 0 COMMENT '排序',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_platform_code (platform_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户平台表';

-- 平台账号表
CREATE TABLE IF NOT EXISTS platform_account (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_platform_id    BIGINT NOT NULL COMMENT '关联用户平台',
    cookie_file_path    VARCHAR(500) NOT NULL COMMENT 'cookie持久化文件路径',
    display_name        VARCHAR(100) DEFAULT NULL COMMENT '账号显示名',
    is_active           TINYINT(1) DEFAULT 1 COMMENT '登录态是否有效',
    last_login_at       DATETIME DEFAULT NULL COMMENT '最后登录时间',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_platform_id (user_platform_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台账号表';

-- AI改写任务表
CREATE TABLE IF NOT EXISTS ai_adapt_task (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    content_id              BIGINT NOT NULL COMMENT '内容ID',
    target_platform_code    VARCHAR(50) NOT NULL COMMENT '目标平台',
    preset_template         VARCHAR(50) DEFAULT NULL COMMENT '预设模板',
    user_prompt             TEXT DEFAULT NULL COMMENT '用户自定义提示词',
    adapted_content         LONGTEXT DEFAULT NULL COMMENT '改写结果',
    tokens_used             INT DEFAULT 0 COMMENT 'Token消耗',
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_content_id (content_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI改写任务表';

-- 风格模板表
CREATE TABLE IF NOT EXISTS style_template (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(100) NOT NULL COMMENT '模板名称',
    icon              VARCHAR(10) DEFAULT '📝' COMMENT '图标',
    description       VARCHAR(200) DEFAULT NULL COMMENT '描述',
    reference_type    VARCHAR(10) DEFAULT 'TEXT' COMMENT 'TEXT/IMAGE',
    reference_sample  TEXT DEFAULT NULL COMMENT '参考文本',
    reference_image   VARCHAR(500) DEFAULT NULL COMMENT '参考图片URL',
    system_prompt     TEXT NOT NULL COMMENT 'System Prompt',
    extracted_style   TEXT DEFAULT NULL COMMENT 'AI提取的风格特征(JSON)',
    is_preset         TINYINT(1) DEFAULT 0 COMMENT '系统预置=1',
    user_id           BIGINT DEFAULT NULL COMMENT '自建模板关联用户',
    order_no          INT DEFAULT 0 COMMENT '排序',
    use_count         INT DEFAULT 0 COMMENT '使用次数',
    created_at        DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_is_preset (is_preset),
    INDEX idx_use_count (use_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风格模板表';

-- 上传文件表
CREATE TABLE IF NOT EXISTS upload_file (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_name     VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_path       VARCHAR(500) NOT NULL COMMENT '存储路径',
    file_type         VARCHAR(20) NOT NULL COMMENT 'IMAGE/VIDEO',
    file_size         BIGINT NOT NULL COMMENT '原始字节数',
    compressed_size   BIGINT DEFAULT NULL COMMENT '压缩后字节数',
    width             INT DEFAULT NULL COMMENT '宽度',
    height            INT DEFAULT NULL COMMENT '高度',
    duration          INT DEFAULT NULL COMMENT '视频时长(秒)',
    thumbnail_path    VARCHAR(500) DEFAULT NULL COMMENT '缩略图路径',
    upload_status     VARCHAR(20) DEFAULT 'UPLOADING' COMMENT 'UPLOADING/COMPRESSING/READY',
    created_at        DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_file_type (file_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='上传文件表';

-- 系统预置平台数据
INSERT IGNORE INTO user_platform (platform_code, platform_name, login_url, editor_url, is_builtin, sort_order) VALUES
('WECHAT',   '微信公众号', 'https://mp.weixin.qq.com/', 'https://mp.weixin.qq.com/cgi-bin/appmsg?t=media/appmsg_edit&action=edit', 1, 1),
('ZHIHU',    '知乎',      'https://www.zhihu.com/signin', 'https://zhuanlan.zhihu.com/write', 1, 2),
('BILIBILI', 'B站专栏',    'https://passport.bilibili.com/login', 'https://member.bilibili.com/platform/upload/text', 1, 3),
('XHS',      '小红书',     'https://creator.xiaohongshu.com/login', 'https://creator.xiaohongshu.com/publish/publish', 1, 4),
('TOUTIAO',  '今日头条',   'https://mp.toutiao.com/login/', 'https://mp.toutiao.com/profile_v4/graphic/publish', 1, 5);

-- 系统预置模板数据
INSERT IGNORE INTO style_template (name, icon, description, reference_type, system_prompt, is_preset, order_no) VALUES
('专业深度', '📝', '结构化分段、信息密度高、客观冷静', 'TEXT',
 '你是资深内容编辑。请将以下文章改写为专业深度风格。规则: 1.使用清晰的小标题分段 2.每段有明确的论点+论据结构 3.保持客观冷静的语调 4.适合深度阅读场景', 1, 1),
('理性论证', '🧠', '开头抛观点、逻辑链完整、引用数据', 'TEXT',
 '你是知乎高赞作者。请将以下文章改写为理性论证风格。规则: 1.开头用一句话抛出核心观点 2.逻辑链完整，有推导过程 3.适当引用数据或参考文献 4.克制但有力，避免情绪化表达', 1, 2),
('网感互动', '🎮', '口语化、"家人们"、调侃、弹幕梗', 'TEXT',
 '你是B站百大UP主。请将以下文章改写为年轻网感风格。规则: 1.使用"家人们"、"懂的都懂"等网感用语 2.嵌入弹幕文化梗 3.语气轻松调侃 4.结尾引导互动', 1, 3),
('种草推荐', '✨', 'emoji密集、短段落、"姐妹们"、#标签', 'TEXT',
 '你是小红书万粉博主。请将以下文章改写为种草推荐风格。规则: 1.大量使用emoji，每段2-3个 2.口语化、闺蜜聊天语气 3.段落不超过3行 4.使用"姐妹们冲鸭!"等后缀 5.正文≤1000字，添加3-5个#话题标签', 1, 4),
('通俗资讯', '📰', '故事开头、短句、接地气、标题悬念', 'TEXT',
 '你是头条号爆款作者。请将以下文章改写为通俗资讯风格。规则: 1.用故事或疑问开头引入 2.短句为主，一段不超过2-3句话 3.口语化表达，接地气 4.标题要有悬念感或反差感', 1, 5);
