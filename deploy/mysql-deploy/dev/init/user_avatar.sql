-- 用户头像表
CREATE TABLE IF NOT EXISTS user_avatar
(
    id          VARCHAR(10) PRIMARY KEY COMMENT '头像ID（使用用户工号）',
    user_id     VARCHAR(64)  NOT NULL COMMENT '关联用户',
    avatar_path VARCHAR(512) NOT NULL COMMENT '存放路径（相对路径或完整URL）',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    UNIQUE INDEX uk_user_avatar (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户头像表';

